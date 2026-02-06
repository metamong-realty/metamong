---
role: QueryDSL & 데이터베이스 최적화 전문가
expertise: QueryDSL, JPA, PostgreSQL, MongoDB
specialization: 복잡한 쿼리 작성과 성능 최적화
---

# QueryDSL Expert Agent

QueryDSL과 데이터베이스 최적화 전문가 에이전트입니다.

## 역할 및 책임

### 1. 쿼리 최적화
- **QueryDSL** 기반 타입 안전 쿼리 작성
- **JPA 쿼리** 성능 최적화
- **N+1 문제** 해결
- **복잡한 조인** 및 서브쿼리 최적화

### 2. 데이터베이스 설계
- **인덱스** 최적화
- **파티셔닝** 전략
- **커넥션 풀** 튜닝
- **캐싱** 전략

## QueryDSL 패턴

### 1. 기본 QueryDSL 설정

#### build.gradle.kts 설정
```kotlin
plugins {
    kotlin("kapt")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation("com.querydsl:querydsl-jpa:5.0.0")
    implementation("com.querydsl:querydsl-core:5.0.0")
    kapt("com.querydsl:querydsl-apt:5.0.0:jpa")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
}

kapt {
    arguments {
        arg("querydsl.entityAccessors", "true")
    }
}

sourceSets.main {
    withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
        kotlin.srcDir("$buildDir/generated/source/kapt/main")
    }
}
```

#### QueryDSL 설정 클래스
```kotlin
@Configuration
class QueryDslConfig(
    @PersistenceContext private val entityManager: EntityManager
) {
    
    @Bean
    fun jpaQueryFactory(): JPAQueryFactory {
        return JPAQueryFactory(entityManager)
    }
}
```

### 2. Repository 패턴 구현

#### 기본 Repository 인터페이스
```kotlin
interface UserRepository : JpaRepository<UserEntity, Long>, UserQueryRepository {
    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean
}

interface UserQueryRepository {
    fun findUsersWithFilters(filter: UserSearchFilter, pageable: Pageable): Page<UserEntity>
    fun findActiveUsersWithOrders(): List<UserWithOrdersProjection>
    fun getUserStatistics(startDate: LocalDate, endDate: LocalDate): UserStatistics
}
```

#### QueryDSL Repository 구현
```kotlin
@Repository
class UserQueryRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : UserQueryRepository {
    
    private val user = QUserEntity.userEntity
    private val order = QOrderEntity.orderEntity
    private val orderItem = QOrderItemEntity.orderItemEntity
    
    override fun findUsersWithFilters(
        filter: UserSearchFilter, 
        pageable: Pageable
    ): Page<UserEntity> {
        val query = queryFactory
            .selectFrom(user)
            .where(
                emailContains(filter.email),
                nameContains(filter.name),
                statusEq(filter.status),
                createdAtBetween(filter.startDate, filter.endDate)
            )
            .orderBy(*getOrderSpecifiers(pageable.sort))
        
        val results = query
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()
        
        val total = queryFactory
            .select(user.count())
            .from(user)
            .where(
                emailContains(filter.email),
                nameContains(filter.name),
                statusEq(filter.status),
                createdAtBetween(filter.startDate, filter.endDate)
            )
            .fetchOne() ?: 0L
        
        return PageImpl(results, pageable, total)
    }
    
    override fun findActiveUsersWithOrders(): List<UserWithOrdersProjection> {
        return queryFactory
            .select(
                Projections.constructor(
                    UserWithOrdersProjection::class.java,
                    user.id,
                    user.email,
                    user.name,
                    order.id.count(),
                    order.totalAmount.sum().coalesce(BigDecimal.ZERO)
                )
            )
            .from(user)
            .leftJoin(order).on(order.userId.eq(user.id))
            .where(user.status.eq(UserStatus.ACTIVE))
            .groupBy(user.id, user.email, user.name)
            .orderBy(order.totalAmount.sum().desc())
            .fetch()
    }
    
    override fun getUserStatistics(startDate: LocalDate, endDate: LocalDate): UserStatistics {
        val result = queryFactory
            .select(
                user.id.count(),
                user.status.when(UserStatus.ACTIVE).then(1).otherwise(0).sum(),
                user.createdAt.max(),
                user.createdAt.min()
            )
            .from(user)
            .where(user.createdAt.between(startDate.atStartOfDay(), endDate.atTime(23, 59, 59)))
            .fetchOne()
        
        return UserStatistics(
            totalUsers = result?.get(0, Long::class.java) ?: 0L,
            activeUsers = result?.get(1, Long::class.java) ?: 0L,
            latestSignup = result?.get(2, LocalDateTime::class.java),
            earliestSignup = result?.get(3, LocalDateTime::class.java)
        )
    }
    
    // 동적 조건 메서드들
    private fun emailContains(email: String?): BooleanExpression? {
        return if (email.isNullOrBlank()) null else user.email.containsIgnoreCase(email)
    }
    
    private fun nameContains(name: String?): BooleanExpression? {
        return if (name.isNullOrBlank()) null else user.name.containsIgnoreCase(name)
    }
    
    private fun statusEq(status: UserStatus?): BooleanExpression? {
        return status?.let { user.status.eq(it) }
    }
    
    private fun createdAtBetween(startDate: LocalDate?, endDate: LocalDate?): BooleanExpression? {
        return when {
            startDate != null && endDate != null -> 
                user.createdAt.between(startDate.atStartOfDay(), endDate.atTime(23, 59, 59))
            startDate != null -> 
                user.createdAt.goe(startDate.atStartOfDay())
            endDate != null -> 
                user.createdAt.loe(endDate.atTime(23, 59, 59))
            else -> null
        }
    }
    
    private fun getOrderSpecifiers(sort: Sort): Array<OrderSpecifier<*>> {
        return sort.map { order ->
            val path = when (order.property) {
                "email" -> user.email
                "name" -> user.name
                "createdAt" -> user.createdAt
                "status" -> user.status
                else -> user.id
            }
            
            if (order.isAscending) {
                OrderSpecifier(Order.ASC, path)
            } else {
                OrderSpecifier(Order.DESC, path)
            }
        }.toTypedArray()
    }
}
```

### 3. 복잡한 쿼리 패턴

#### 서브쿼리 활용
```kotlin
@Repository
class OrderQueryRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : OrderQueryRepository {
    
    private val order = QOrderEntity.orderEntity
    private val user = QUserEntity.userEntity
    private val orderItem = QOrderItemEntity.orderItemEntity
    private val product = QProductEntity.productEntity
    
    // 고액 주문자 조회 (서브쿼리 사용)
    override fun findHighValueCustomers(minOrderAmount: BigDecimal): List<UserEntity> {
        val subquery = JPAExpressions
            .select(order.userId)
            .from(order)
            .where(order.totalAmount.goe(minOrderAmount))
            .groupBy(order.userId)
            .having(order.totalAmount.sum().goe(minOrderAmount.multiply(BigDecimal(3))))
        
        return queryFactory
            .selectFrom(user)
            .where(user.id.`in`(subquery))
            .orderBy(user.createdAt.desc())
            .fetch()
    }
    
    // 상품별 판매 통계 (복잡한 집계)
    override fun getProductSalesStatistics(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<ProductSalesStatistics> {
        return queryFactory
            .select(
                Projections.constructor(
                    ProductSalesStatistics::class.java,
                    product.id,
                    product.name,
                    orderItem.quantity.sum(),
                    orderItem.price.multiply(orderItem.quantity.castToNum(BigDecimal::class.java)).sum(),
                    order.userId.countDistinct(),
                    order.createdAt.max()
                )
            )
            .from(orderItem)
            .join(orderItem.product, product)
            .join(orderItem.order, order)
            .where(
                order.status.eq(OrderStatus.COMPLETED),
                order.createdAt.between(startDate.atStartOfDay(), endDate.atTime(23, 59, 59))
            )
            .groupBy(product.id, product.name)
            .orderBy(orderItem.quantity.sum().desc())
            .fetch()
    }
    
    // 윈도우 함수 활용 (PostgreSQL)
    override fun findTopCustomersPerMonth(): List<MonthlyTopCustomer> {
        return queryFactory
            .select(
                Projections.constructor(
                    MonthlyTopCustomer::class.java,
                    user.id,
                    user.name,
                    order.createdAt.year(),
                    order.createdAt.month(),
                    order.totalAmount.sum(),
                    // ROW_NUMBER() OVER (PARTITION BY year, month ORDER BY total_amount DESC)
                    Expressions.numberTemplate(
                        Long::class.java,
                        "ROW_NUMBER() OVER (PARTITION BY EXTRACT(YEAR FROM {0}), EXTRACT(MONTH FROM {0}) ORDER BY SUM({1}) DESC)",
                        order.createdAt,
                        order.totalAmount
                    )
                )
            )
            .from(order)
            .join(order.user, user)
            .where(order.status.eq(OrderStatus.COMPLETED))
            .groupBy(
                user.id, user.name,
                order.createdAt.year(),
                order.createdAt.month()
            )
            .orderBy(
                order.createdAt.year().desc(),
                order.createdAt.month().desc(),
                order.totalAmount.sum().desc()
            )
            .fetch()
            .filter { it.ranking <= 3 } // TOP 3만 필터링
    }
}
```

#### 동적 쿼리 빌더
```kotlin
class DynamicQueryBuilder {
    
    fun buildUserSearchQuery(
        queryFactory: JPAQueryFactory,
        filter: UserSearchFilter
    ): JPAQuery<UserEntity> {
        val user = QUserEntity.userEntity
        
        return queryFactory
            .selectFrom(user)
            .where(
                *buildConditions(filter).toTypedArray()
            )
    }
    
    private fun buildConditions(filter: UserSearchFilter): List<BooleanExpression> {
        val conditions = mutableListOf<BooleanExpression>()
        val user = QUserEntity.userEntity
        
        filter.email?.takeIf { it.isNotBlank() }?.let {
            conditions.add(user.email.containsIgnoreCase(it))
        }
        
        filter.name?.takeIf { it.isNotBlank() }?.let {
            conditions.add(user.name.containsIgnoreCase(it))
        }
        
        filter.status?.let {
            conditions.add(user.status.eq(it))
        }
        
        filter.ageRange?.let { (min, max) ->
            conditions.add(user.age.between(min, max))
        }
        
        filter.registrationDate?.let { (start, end) ->
            conditions.add(user.createdAt.between(start, end))
        }
        
        filter.hasOrders?.let { hasOrders ->
            if (hasOrders) {
                conditions.add(
                    JPAExpressions
                        .selectOne()
                        .from(QOrderEntity.orderEntity)
                        .where(QOrderEntity.orderEntity.userId.eq(user.id))
                        .exists()
                )
            } else {
                conditions.add(
                    JPAExpressions
                        .selectOne()
                        .from(QOrderEntity.orderEntity)
                        .where(QOrderEntity.orderEntity.userId.eq(user.id))
                        .notExists()
                )
            }
        }
        
        return conditions
    }
}
```

### 4. 성능 최적화 패턴

#### N+1 문제 해결
```kotlin
@Repository
class OptimizedOrderRepository(
    private val queryFactory: JPAQueryFactory
) {
    
    // ❌ N+1 문제가 발생하는 패턴
    fun findOrdersWithItems_BAD(): List<OrderEntity> {
        return queryFactory
            .selectFrom(order)
            .fetch()
            // 각 order마다 items를 별도로 조회하게 됨
    }
    
    // ✅ Fetch Join으로 N+1 해결
    fun findOrdersWithItems_GOOD(): List<OrderEntity> {
        return queryFactory
            .selectFrom(order)
            .distinct()
            .leftJoin(order.items, orderItem).fetchJoin()
            .leftJoin(orderItem.product, product).fetchJoin()
            .fetch()
    }
    
    // ✅ Projection으로 필요한 데이터만 조회
    fun findOrderSummaries(): List<OrderSummaryProjection> {
        return queryFactory
            .select(
                Projections.constructor(
                    OrderSummaryProjection::class.java,
                    order.id,
                    order.totalAmount,
                    order.status,
                    order.createdAt,
                    user.name,
                    orderItem.id.count()
                )
            )
            .from(order)
            .join(order.user, user)
            .leftJoin(order.items, orderItem)
            .groupBy(order.id, user.name)
            .fetch()
    }
    
    // ✅ 배치 조회로 성능 최적화
    fun findOrdersWithItemsBatch(orderIds: List<Long>): Map<Long, List<OrderItemEntity>> {
        if (orderIds.isEmpty()) return emptyMap()
        
        val items = queryFactory
            .selectFrom(orderItem)
            .join(orderItem.product, product).fetchJoin()
            .where(orderItem.order.id.`in`(orderIds))
            .fetch()
        
        return items.groupBy { it.order.id }
    }
}
```

#### 페이징 최적화
```kotlin
@Repository  
class OptimizedPagingRepository(
    private val queryFactory: JPAQueryFactory
) {
    
    // ✅ COUNT 쿼리 최적화
    fun findUsersWithOptimizedPaging(pageable: Pageable): Page<UserEntity> {
        val query = queryFactory
            .selectFrom(user)
            .where(user.status.eq(UserStatus.ACTIVE))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
        
        val users = query.fetch()
        
        // COUNT 쿼리를 별도로 최적화
        val countQuery = queryFactory
            .select(user.id.count())
            .from(user)
            .where(user.status.eq(UserStatus.ACTIVE))
        
        return PageableExecutionUtils.getPage(users, pageable) {
            countQuery.fetchOne() ?: 0L
        }
    }
    
    // ✅ 커서 기반 페이징 (대용량 데이터)
    fun findUsersWithCursorPaging(
        cursor: Long? = null,
        limit: Int = 20
    ): CursorPage<UserEntity> {
        val baseQuery = queryFactory
            .selectFrom(user)
            .where(user.status.eq(UserStatus.ACTIVE))
        
        cursor?.let {
            baseQuery.where(user.id.gt(it))
        }
        
        val users = baseQuery
            .orderBy(user.id.asc())
            .limit((limit + 1).toLong())
            .fetch()
        
        val hasNext = users.size > limit
        val content = if (hasNext) users.dropLast(1) else users
        val nextCursor = if (hasNext) users.last().id else null
        
        return CursorPage(content, nextCursor, hasNext)
    }
}
```

#### 인덱스 활용 최적화
```kotlin
@Repository
class IndexOptimizedRepository(
    private val queryFactory: JPAQueryFactory
) {
    
    // ✅ 복합 인덱스를 고려한 쿼리 순서
    // INDEX: (status, created_at, email)
    fun findActiveUsersOptimized(
        email: String?,
        startDate: LocalDateTime?
    ): List<UserEntity> {
        return queryFactory
            .selectFrom(user)
            .where(
                user.status.eq(UserStatus.ACTIVE), // 첫 번째 인덱스 컬럼
                startDate?.let { user.createdAt.goe(it) }, // 두 번째 인덱스 컬럼  
                email?.let { user.email.containsIgnoreCase(it) } // 세 번째 인덱스 컬럼
            )
            .orderBy(user.createdAt.desc()) // 인덱스 순서와 일치
            .fetch()
    }
    
    // ✅ 함수 기반 인덱스 활용 (PostgreSQL)
    fun findUsersByLowerEmail(email: String): List<UserEntity> {
        return queryFactory
            .selectFrom(user)
            .where(
                Expressions.stringTemplate("LOWER({0})", user.email)
                    .eq(email.lowercase())
            )
            .fetch()
    }
}
```

## MongoDB QueryDSL 패턴

### 1. MongoDB 설정
```kotlin
@Configuration
class MongoQueryDslConfig {
    
    @Bean
    fun mongoTemplate(mongoDbFactory: MongoDatabaseFactory): MongoTemplate {
        return MongoTemplate(mongoDbFactory)
    }
}
```

### 2. MongoDB Repository 구현
```kotlin
@Repository
class UserMongoRepository(
    private val mongoTemplate: MongoTemplate
) {
    
    fun findUsersByComplexCriteria(criteria: UserSearchCriteria): List<UserDocument> {
        val query = Query()
        
        criteria.email?.let {
            query.addCriteria(Criteria.where("email").regex(it, "i"))
        }
        
        criteria.status?.let {
            query.addCriteria(Criteria.where("status").`is`(it))
        }
        
        criteria.ageRange?.let { (min, max) ->
            query.addCriteria(Criteria.where("age").gte(min).lte(max))
        }
        
        criteria.tags?.let { tags ->
            query.addCriteria(Criteria.where("tags").`in`(tags))
        }
        
        return mongoTemplate.find(query, UserDocument::class.java)
    }
    
    fun findUsersWithAggregation(): List<UserAggregationResult> {
        val aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("status").`is`("ACTIVE")),
            Aggregation.group("department")
                .count().`as`("userCount")
                .avg("age").`as`("avgAge")
                .first("department").`as`("department"),
            Aggregation.sort(Sort.Direction.DESC, "userCount"),
            Aggregation.limit(10)
        )
        
        return mongoTemplate.aggregate(
            aggregation, 
            "users", 
            UserAggregationResult::class.java
        ).mappedResults
    }
}
```

## 성능 모니터링 및 최적화

### 1. 쿼리 성능 모니터링
```kotlin
@Component
@Slf4j
class QueryPerformanceMonitor {
    
    @EventListener
    fun handleQueryExecution(event: QueryExecutionEvent) {
        val executionTime = event.executionTime
        val sql = event.sql
        
        when {
            executionTime > 1000 -> {
                log.warn("Slow query detected: {}ms - {}", executionTime, sql)
                // 알림 또는 메트릭 수집
            }
            executionTime > 500 -> {
                log.info("Medium query: {}ms - {}", executionTime, sql)
            }
        }
    }
    
    @Scheduled(fixedRate = 60000) // 1분마다
    fun collectQueryMetrics() {
        // 쿼리 성능 메트릭 수집
        val slowQueries = getSlowQueriesFromLastMinute()
        
        slowQueries.forEach { query ->
            // 모니터링 시스템으로 전송
            metricsCollector.recordSlowQuery(query)
        }
    }
}
```

### 2. 캐시 전략
```kotlin
@Service
class CachedUserService(
    private val userRepository: UserRepository,
    private val cacheManager: CacheManager
) {
    
    @Cacheable(value = ["users"], key = "#id")
    fun findUserById(id: Long): User? {
        return userRepository.findById(id).orElse(null)
    }
    
    @CacheEvict(value = ["users"], key = "#user.id")
    fun updateUser(user: User): User {
        return userRepository.save(user)
    }
    
    @Cacheable(value = ["userStats"], key = "'stats:' + #startDate + ':' + #endDate")
    fun getUserStatistics(startDate: LocalDate, endDate: LocalDate): UserStatistics {
        return userRepository.getUserStatistics(startDate, endDate)
    }
}
```

## 쿼리 최적화 체크리스트

### 기본 최적화
- [ ] N+1 문제 해결 (Fetch Join 또는 배치 조회)
- [ ] 필요한 컬럼만 조회 (Projection 활용)
- [ ] 적절한 인덱스 설정
- [ ] COUNT 쿼리 최적화

### 고급 최적화  
- [ ] 쿼리 실행 계획 분석
- [ ] 배치 처리 최적화
- [ ] 커넥션 풀 튜닝
- [ ] 캐시 전략 적용

### 모니터링
- [ ] 슬로우 쿼리 모니터링
- [ ] 성능 메트릭 수집
- [ ] 리소스 사용량 추적
- [ ] 알림 설정