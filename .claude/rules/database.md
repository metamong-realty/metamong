# Database Guidelines

## JPA Entity 규칙

### Entity 기본 규칙
```kotlin
@Entity
@Table(name = "users")  // 복수형 테이블명
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(unique = true, nullable = false)
    val email: String,
    
    var nickname: String,
    
    @Enumerated(EnumType.STRING)  // 필수: STRING 타입
    val status: UserStatus = UserStatus.ACTIVE,
    
    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    // 연관관계 매핑 금지 - QueryDSL로 처리
}
```

### 네이밍 규칙
- 테이블명: lower_snake_case, 복수형
- 컬럼명: lower_snake_case
- Entity명: PascalCase, 단수형

### @Column 사용 규칙
```kotlin
// Good - 이름이 다를 때만 사용
@Column(name = "user_type")
val type: String

// Bad - 불필요한 어노테이션
@Column(nullable = false, length = 255)  // DB에서만 관리
val email: String
```

### Enum 매핑
```kotlin
enum class UserStatus {
    ACTIVE, INACTIVE, SUSPENDED
}

// Entity에서 사용
@Enumerated(EnumType.STRING)  // 반드시 STRING
val status: UserStatus
```

### Auditing 설정
```kotlin
@EntityListeners(AuditingEntityListener::class)
@MappedSuperclass
abstract class BaseEntity(
    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @CreatedBy
    val createdBy: String? = null,
    
    @LastModifiedBy
    var updatedBy: String? = null
)
```

## QueryDSL 사용법

### Repository 구조
```kotlin
// JPA Repository
interface UserJpaRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
}

// QueryDSL Repository
interface UserQueryRepository {
    fun findActiveUsersWithPosts(pageable: Pageable): Page<UserWithPostsProjection>
    fun findUserStatistics(userId: Long): UserStatisticsProjection?
}

// QueryDSL 구현
@Repository
class UserQueryRepositoryImpl : UserQueryRepository, QuerydslRepositorySupport(User::class.java) {
    
    private val user = QUser.user
    private val post = QPost.post
    
    override fun findActiveUsersWithPosts(pageable: Pageable): Page<UserWithPostsProjection> {
        val query = from(user)
            .leftJoin(post).on(post.userId.eq(user.id))
            .where(user.status.eq(UserStatus.ACTIVE))
            .groupBy(user.id)
            
        val results = querydsl!!.applyPagination(pageable, query)
            .select(
                Projections.constructor(
                    UserWithPostsProjection::class.java,
                    user.id,
                    user.email,
                    user.nickname,
                    post.count()
                )
            )
            .fetch()
            
        val total = query.fetchCount()
        
        return PageImpl(results, pageable, total)
    }
}
```

### Projection 사용
```kotlin
// DTO Projection
data class UserWithPostsProjection(
    val id: Long,
    val email: String,
    val nickname: String,
    val postCount: Long
)

// QueryDSL에서 사용
.select(
    Projections.constructor(
        UserWithPostsProjection::class.java,
        user.id,
        user.email,
        user.nickname,
        post.count()
    )
)
```

### 동적 쿼리
```kotlin
fun searchUsers(
    keyword: String?,
    status: UserStatus?,
    pageable: Pageable
): Page<User> {
    val builder = BooleanBuilder()
    
    keyword?.let {
        builder.and(
            user.nickname.contains(it)
                .or(user.email.contains(it))
        )
    }
    
    status?.let {
        builder.and(user.status.eq(it))
    }
    
    return findAll(builder, pageable)
}
```

### N+1 문제 해결
```kotlin
// Fetch Join 사용
fun findUsersWithPosts(): List<User> {
    return from(user)
        .leftJoin(post).on(post.userId.eq(user.id))
        .fetchJoin()  // N+1 방지
        .distinct()
        .fetch()
}
```

## MongoDB 사용법

### Document 정의
```kotlin
@Document(collection = "user_activities")
data class UserActivity(
    @Id
    val id: String = ObjectId().toString(),
    
    @Indexed
    val userId: Long,
    
    val activityType: String,
    
    val metadata: Map<String, Any> = emptyMap(),
    
    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now()
)
```

### Repository
```kotlin
interface UserActivityRepository : MongoRepository<UserActivity, String> {
    fun findByUserId(userId: Long): List<UserActivity>
    
    @Query("{ 'userId': ?0, 'activityType': ?1 }")
    fun findByUserIdAndType(userId: Long, type: String): List<UserActivity>
}
```

### 복잡한 쿼리
```kotlin
@Repository
class UserActivityCustomRepository(
    private val mongoTemplate: MongoTemplate
) {
    fun aggregateUserActivities(userId: Long): List<ActivitySummary> {
        val aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("userId").`is`(userId)),
            Aggregation.group("activityType")
                .count().`as`("count")
                .first("createdAt").`as`("firstActivity")
                .last("createdAt").`as`("lastActivity"),
            Aggregation.sort(Sort.Direction.DESC, "count")
        )
        
        return mongoTemplate.aggregate(
            aggregation,
            UserActivity::class.java,
            ActivitySummary::class.java
        ).mappedResults
    }
}
```

## Redis 사용법

### 캐시 설정
```kotlin
@Configuration
@EnableCaching
class RedisConfig {
    
    @Bean
    fun cacheManager(
        redisConnectionFactory: RedisConnectionFactory
    ): CacheManager {
        val config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(GenericJackson2JsonRedisSerializer())
            )
            
        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(config)
            .build()
    }
}
```

### 캐시 사용
```kotlin
@Service
class UserService(
    private val userRepository: UserRepository
) {
    @Cacheable(value = ["users"], key = "#id")
    fun getUser(id: Long): User? {
        return userRepository.findByIdOrNull(id)
    }
    
    @CacheEvict(value = ["users"], key = "#user.id")
    fun updateUser(user: User): User {
        return userRepository.save(user)
    }
    
    @CacheEvict(value = ["users"], allEntries = true)
    fun clearAllUserCache() {
        // 캐시 전체 삭제
    }
}
```

### RedisTemplate 직접 사용
```kotlin
@Service
class RedisService(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    fun <T> setValue(key: String, value: T, timeout: Duration = Duration.ofHours(1)) {
        redisTemplate.opsForValue().set(key, value, timeout)
    }
    
    fun <T> getValue(key: String, clazz: Class<T>): T? {
        val value = redisTemplate.opsForValue().get(key)
        return objectMapper.convertValue(value, clazz)
    }
    
    fun setList(key: String, values: List<Any>) {
        redisTemplate.opsForList().rightPushAll(key, *values.toTypedArray())
    }
    
    fun getList(key: String): List<Any> {
        return redisTemplate.opsForList().range(key, 0, -1) ?: emptyList()
    }
}
```

### 분산 락
```kotlin
@Component
class RedisLockService(
    private val redissonClient: RedissonClient
) {
    fun <T> executeWithLock(
        key: String,
        waitTime: Long = 3,
        leaseTime: Long = 5,
        unit: TimeUnit = TimeUnit.SECONDS,
        action: () -> T
    ): T {
        val lock = redissonClient.getLock(key)
        
        try {
            val acquired = lock.tryLock(waitTime, leaseTime, unit)
            if (!acquired) {
                throw LockAcquisitionException("Failed to acquire lock: $key")
            }
            return action()
        } finally {
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
            }
        }
    }
}

// 사용 예시
@Service
class PaymentService(
    private val redisLockService: RedisLockService
) {
    fun processPayment(orderId: Long) {
        redisLockService.executeWithLock("payment:$orderId") {
            // 중복 결제 방지 로직
        }
    }
}
```

## 트랜잭션 관리

### 기본 트랜잭션
```kotlin
@Service
@Transactional
class OrderService {
    fun createOrder(request: CreateOrderRequest): Order {
        // 모든 메서드가 트랜잭션 내에서 실행
    }
    
    @Transactional(readOnly = true)
    fun getOrder(id: Long): Order? {
        // 읽기 전용 트랜잭션
    }
    
    @Transactional(
        propagation = Propagation.REQUIRES_NEW,
        isolation = Isolation.SERIALIZABLE
    )
    fun processPayment(order: Order) {
        // 새로운 트랜잭션에서 실행
    }
}
```

### 트랜잭션 이벤트
```kotlin
@Service
class OrderEventService {
    
    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleOrderCreated(event: OrderCreatedEvent) {
        // 트랜잭션 커밋 후 실행
    }
}
```

## 마이그레이션

### Flyway 설정
```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

### 마이그레이션 파일
```sql
-- V1__create_users_table.sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
```

## 성능 최적화

### 인덱스 전략
```kotlin
@Table(
    indexes = [
        Index(name = "idx_user_email", columnList = "email"),
        Index(name = "idx_user_status", columnList = "status"),
        Index(name = "idx_user_created", columnList = "created_at")
    ]
)
@Entity
class User
```

### Batch Insert
```kotlin
@Repository
class UserBatchRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    fun batchInsert(users: List<User>) {
        val sql = """
            INSERT INTO users (email, nickname, status, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()
        
        jdbcTemplate.batchUpdate(
            sql,
            users,
            users.size
        ) { ps, user ->
            ps.setString(1, user.email)
            ps.setString(2, user.nickname)
            ps.setString(3, user.status.name)
            ps.setTimestamp(4, Timestamp.valueOf(user.createdAt))
            ps.setTimestamp(5, Timestamp.valueOf(user.updatedAt))
        }
    }
}
```

### Connection Pool 설정
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
      idle-timeout: 300000
      connection-timeout: 20000
      max-lifetime: 1200000
```

## 데이터베이스 규칙 체크리스트

- [ ] Entity에 @Enumerated(EnumType.STRING) 사용
- [ ] 연관관계 매핑 대신 QueryDSL 사용
- [ ] N+1 문제 체크
- [ ] 인덱스 적절히 설정
- [ ] 트랜잭션 범위 최소화
- [ ] 캐시 전략 수립
- [ ] 페이징 처리
- [ ] Projection 활용