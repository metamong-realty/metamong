---
name: repository-pattern
description: Repository 패턴 가이드. JPA, QueryDSL, MongoDB 데이터 액세스 구현 시 참조
---

# Repository 패턴 가이드

Metamong 프로젝트의 데이터 액세스 계층 구현 규칙입니다.

## 🚫 절대 금지 사항

### @Query 애노테이션 사용 금지
```kotlin
// ❌ 절대 사용 금지
@Query("SELECT u FROM User u WHERE u.email = :email")
fun findByEmail(email: String): User?

// ❌ Native Query도 금지
@Query(value = "SELECT * FROM users WHERE email = ?1", nativeQuery = true)
fun findByEmailNative(email: String): User?
```

**이유**: 
- 컴파일 타임 타입 안정성 부족
- 리팩토링 시 깨지기 쉬움
- IDE 지원 부족

---

## ✅ 권장 방법

### 1. Spring Data Method Naming

가장 간단한 쿼리는 메소드 이름으로 해결:

```kotlin
interface UserRepository : JpaRepository<User, Long> {
    // 단순 조회
    fun findByEmail(email: String): User?
    fun findByNameAndAge(name: String, age: Int): List<User>
    fun existsByEmail(email: String): Boolean
    fun countByStatus(status: UserStatus): Long
    
    // 정렬
    fun findByStatusOrderByCreatedAtDesc(status: UserStatus): List<User>
    
    // 제한
    fun findTop10ByStatusOrderByCreatedAtDesc(status: UserStatus): List<User>
    
    // 비교 연산자
    fun findByAgeGreaterThan(age: Int): List<User>
    fun findByCreatedAtBetween(start: LocalDateTime, end: LocalDateTime): List<User>
    
    // IN 절
    fun findByIdIn(ids: List<Long>): List<User>
    
    // LIKE
    fun findByNameContaining(keyword: String): List<User>
    fun findByEmailStartingWith(prefix: String): List<User>
    
    // NULL 체크
    fun findByDeletedAtIsNull(): List<User>
    fun findByDeletedAtIsNotNull(): List<User>
}
```

### 2. QueryDSL for Complex Queries

복잡한 동적 쿼리는 QueryDSL 사용:

#### Custom Repository 패턴

```kotlin
// 1. Custom 인터페이스 정의
interface UserRepositoryCustom {
    fun searchUsers(condition: UserSearchCondition): Page<User>
    fun findUsersWithOrders(): List<UserWithOrdersDto>
    fun updateBulkStatus(ids: List<Long>, status: UserStatus): Long
}

// 2. QueryDSL 구현
@Repository
class UserRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
) : UserRepositoryCustom {
    
    override fun searchUsers(condition: UserSearchCondition): Page<User> {
        val query = queryFactory
            .selectFrom(user)
            .where(
                nameContains(condition.name),
                emailContains(condition.email),
                ageGoe(condition.minAge),
                ageLoe(condition.maxAge),
                statusEq(condition.status),
            )
        
        val total = query.fetchCount()
        
        val results = query
            .offset(condition.pageable.offset)
            .limit(condition.pageable.pageSize)
            .orderBy(user.createdAt.desc())
            .fetch()
        
        return PageImpl(results, condition.pageable, total)
    }
    
    override fun findUsersWithOrders(): List<UserWithOrdersDto> {
        return queryFactory
            .select(
                QUserWithOrdersDto(
                    user.id,
                    user.name,
                    user.email,
                    order.count(),
                )
            )
            .from(user)
            .leftJoin(order).on(order.userId.eq(user.id))
            .groupBy(user.id)
            .fetch()
    }
    
    override fun updateBulkStatus(ids: List<Long>, status: UserStatus): Long {
        return queryFactory
            .update(user)
            .set(user.status, status)
            .set(user.updatedAt, LocalDateTime.now())
            .where(user.id.`in`(ids))
            .execute()
    }
    
    // 동적 조건 메서드들
    private fun nameContains(name: String?): BooleanExpression? {
        return name?.let { user.name.contains(it) }
    }
    
    private fun emailContains(email: String?): BooleanExpression? {
        return email?.let { user.email.contains(it) }
    }
    
    private fun ageGoe(minAge: Int?): BooleanExpression? {
        return minAge?.let { user.age.goe(it) }
    }
    
    private fun ageLoe(maxAge: Int?): BooleanExpression? {
        return maxAge?.let { user.age.loe(it) }
    }
    
    private fun statusEq(status: UserStatus?): BooleanExpression? {
        return status?.let { user.status.eq(it) }
    }
}

// 3. Main Repository에서 Custom 상속
interface UserRepository : JpaRepository<User, Long>, UserRepositoryCustom
```

#### QueryDSL Projection

```kotlin
// DTO Projection
@QueryProjection
data class UserWithOrdersDto(
    val userId: Long,
    val userName: String,
    val email: String,
    val orderCount: Long,
)

// 사용
val result = queryFactory
    .select(
        QUserWithOrdersDto(
            user.id,
            user.name,
            user.email,
            order.count(),
        )
    )
    .from(user)
    .leftJoin(order).on(order.userId.eq(user.id))
    .groupBy(user.id)
    .fetch()
```

#### 페이징과 정렬

```kotlin
fun searchWithPaging(
    condition: SearchCondition,
    pageable: Pageable,
): Page<User> {
    val query = queryFactory
        .selectFrom(user)
        .where(/* 조건들 */)
    
    // 정렬 적용
    pageable.sort.forEach { sort ->
        val path = PathBuilder(user.type, user.metadata)
        query.orderBy(
            if (sort.isAscending) {
                path.getString(sort.property).asc()
            } else {
                path.getString(sort.property).desc()
            }
        )
    }
    
    // 페이징 적용
    val results = query
        .offset(pageable.offset)
        .limit(pageable.pageSize)
        .fetch()
    
    val total = queryFactory
        .select(user.count())
        .from(user)
        .where(/* 동일 조건 */)
        .fetchOne() ?: 0L
    
    return PageImpl(results, pageable, total)
}
```

### 3. MongoDB Repository

MongoDB는 Spring Data MongoDB의 메소드 네이밍 사용:

```kotlin
interface ArticleRepository : MongoRepository<Article, String> {
    // Method Naming
    fun findByTitleContaining(keyword: String): List<Article>
    fun findByAuthorAndPublishedAtAfter(
        author: String,
        date: LocalDateTime,
    ): List<Article>
    fun findByTagsIn(tags: List<String>): List<Article>
    
    // 복잡한 쿼리는 MongoTemplate 사용
}

// MongoTemplate을 이용한 Custom Repository
@Repository
class ArticleRepositoryCustomImpl(
    private val mongoTemplate: MongoTemplate,
) : ArticleRepositoryCustom {
    
    override fun searchArticles(condition: ArticleSearchCondition): List<Article> {
        val query = Query()
        
        condition.title?.let {
            query.addCriteria(Criteria.where("title").regex(it, "i"))
        }
        
        condition.tags?.let {
            query.addCriteria(Criteria.where("tags").`in`(it))
        }
        
        condition.dateRange?.let { range ->
            query.addCriteria(
                Criteria.where("publishedAt")
                    .gte(range.start)
                    .lte(range.end)
            )
        }
        
        return mongoTemplate.find(query, Article::class.java)
    }
    
    override fun aggregateByAuthor(): List<AuthorStats> {
        val aggregation = Aggregation.newAggregation(
            Aggregation.group("author")
                .count().`as`("articleCount")
                .sum("viewCount").`as`("totalViews"),
            Aggregation.sort(Sort.Direction.DESC, "totalViews"),
        )
        
        return mongoTemplate.aggregate(
            aggregation,
            "articles",
            AuthorStats::class.java,
        ).mappedResults
    }
}
```

---

## 성능 최적화

### N+1 문제 해결

```kotlin
// Fetch Join 사용 (QueryDSL)
fun findUsersWithOrders(): List<User> {
    return queryFactory
        .selectFrom(user)
        .leftJoin(user.orders, order).fetchJoin()
        .distinct()
        .fetch()
}

// @EntityGraph 사용 (JPA)
interface UserRepository : JpaRepository<User, Long> {
    @EntityGraph(attributePaths = ["orders", "profile"])
    fun findWithOrdersById(id: Long): User?
}
```

### Batch Insert

```kotlin
@Repository
class UserBatchRepository(
    private val jdbcTemplate: JdbcTemplate,
) {
    fun saveAll(users: List<User>) {
        val sql = """
            INSERT INTO users (name, email, created_at)
            VALUES (?, ?, ?)
        """.trimIndent()
        
        jdbcTemplate.batchUpdate(
            sql,
            users,
            users.size,
        ) { ps, user ->
            ps.setString(1, user.name)
            ps.setString(2, user.email)
            ps.setTimestamp(3, Timestamp.valueOf(user.createdAt))
        }
    }
}
```

### 읽기 전용 트랜잭션

```kotlin
@Service
class UserQueryService(
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)  // 성능 최적화
    fun searchUsers(condition: UserSearchCondition): Page<User> {
        return userRepository.searchUsers(condition)
    }
}
```

---

## 테스트

### Repository 테스트

```kotlin
@DataJpaTest
class UserRepositoryTest {
    @Autowired
    lateinit var userRepository: UserRepository
    
    @Test
    fun `이메일로 사용자 조회`() {
        // given
        val user = User(
            name = "John",
            email = "john@example.com",
        )
        userRepository.save(user)
        
        // when
        val found = userRepository.findByEmail("john@example.com")
        
        // then
        found shouldNotBe null
        found?.name shouldBe "John"
    }
}
```

### QueryDSL 테스트

```kotlin
@DataJpaTest
@Import(QueryDslConfig::class)
class UserRepositoryCustomTest {
    @Autowired
    lateinit var userRepository: UserRepository
    
    @Test
    fun `복잡한 조건으로 검색`() {
        // given
        createTestUsers()
        
        val condition = UserSearchCondition(
            name = "John",
            minAge = 20,
            maxAge = 30,
            status = UserStatus.ACTIVE,
        )
        
        // when
        val results = userRepository.searchUsers(condition)
        
        // then
        results.content shouldHaveSize 2
        results.content.all { it.status == UserStatus.ACTIVE } shouldBe true
    }
}
```

---

## Best Practices

1. **인터페이스 분리**: 읽기용과 쓰기용 Repository 분리 고려
2. **DTO 프로젝션**: Entity 전체가 아닌 필요한 필드만 조회
3. **Batch 처리**: 대량 데이터는 Batch Insert/Update 사용
4. **캐싱**: 자주 조회되는 데이터는 Redis 캐싱 적용
5. **인덱스 최적화**: 쿼리 패턴에 맞는 인덱스 설계