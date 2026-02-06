---
skill: QueryDSL 쿼리 패턴 라이브러리
category: Data Access
patterns: Dynamic Query, Projection, Subquery, Optimization
---

# QueryDSL Patterns Skill

QueryDSL을 활용한 효율적이고 타입 안전한 쿼리 작성 패턴 라이브러리입니다.

## 기본 설정 및 구조

### QueryDSL 설정 클래스
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

// Repository 베이스 클래스
@Repository
abstract class BaseQueryRepository(
    protected val queryFactory: JPAQueryFactory
) {
    
    // 공통 정렬 변환
    protected fun getOrderSpecifiers(sort: Sort, qEntity: EntityPath<*>): Array<OrderSpecifier<*>> {
        return sort.map { order ->
            val path = getPath(qEntity, order.property)
            if (order.isAscending) {
                OrderSpecifier(Order.ASC, path)
            } else {
                OrderSpecifier(Order.DESC, path)
            }
        }.toTypedArray()
    }
    
    private fun getPath(qEntity: EntityPath<*>, property: String): Path<*> {
        try {
            val field = qEntity.javaClass.getDeclaredField(property)
            field.isAccessible = true
            return field.get(qEntity) as Path<*>
        } catch (e: NoSuchFieldException) {
            // 기본적으로 id 필드 반환
            return qEntity.getMetadata().name
        }
    }
    
    // 페이징 최적화
    protected fun <T> createPage(
        query: JPAQuery<T>,
        countQuery: JPAQuery<Long>,
        pageable: Pageable
    ): Page<T> {
        val results = query
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()
        
        return PageableExecutionUtils.getPage(results, pageable) {
            countQuery.fetchOne() ?: 0L
        }
    }
}
```

### 동적 조건 빌더
```kotlin
class DynamicConditionBuilder {
    
    // 문자열 조건
    fun stringContains(path: StringPath?, value: String?): BooleanExpression? {
        return if (!value.isNullOrBlank()) path?.containsIgnoreCase(value) else null
    }
    
    fun stringEquals(path: StringPath?, value: String?): BooleanExpression? {
        return if (!value.isNullOrBlank()) path?.eq(value) else null
    }
    
    fun stringIn(path: StringPath?, values: List<String>?): BooleanExpression? {
        return if (!values.isNullOrEmpty()) path?.`in`(values) else null
    }
    
    // 숫자 조건
    fun <T : Number & Comparable<T>> numberEquals(
        path: NumberPath<T>?, 
        value: T?
    ): BooleanExpression? {
        return value?.let { path?.eq(it) }
    }
    
    fun <T : Number & Comparable<T>> numberBetween(
        path: NumberPath<T>?, 
        min: T?, 
        max: T?
    ): BooleanExpression? {
        return when {
            min != null && max != null -> path?.between(min, max)
            min != null -> path?.goe(min)
            max != null -> path?.loe(max)
            else -> null
        }
    }
    
    // 날짜 조건
    fun dateBetween(
        path: DateTimePath<LocalDateTime>?, 
        startDate: LocalDate?, 
        endDate: LocalDate?
    ): BooleanExpression? {
        return when {
            startDate != null && endDate != null -> 
                path?.between(startDate.atStartOfDay(), endDate.atTime(23, 59, 59))
            startDate != null -> 
                path?.goe(startDate.atStartOfDay())
            endDate != null -> 
                path?.loe(endDate.atTime(23, 59, 59))
            else -> null
        }
    }
    
    fun dateTimeAfter(
        path: DateTimePath<LocalDateTime>?, 
        after: LocalDateTime?
    ): BooleanExpression? {
        return after?.let { path?.after(it) }
    }
    
    fun dateTimeBefore(
        path: DateTimePath<LocalDateTime>?, 
        before: LocalDateTime?
    ): BooleanExpression? {
        return before?.let { path?.before(it) }
    }
    
    // Enum 조건
    fun <T : Enum<T>> enumEquals(
        path: EnumPath<T>?, 
        value: T?
    ): BooleanExpression? {
        return value?.let { path?.eq(it) }
    }
    
    fun <T : Enum<T>> enumIn(
        path: EnumPath<T>?, 
        values: List<T>?
    ): BooleanExpression? {
        return if (!values.isNullOrEmpty()) path?.`in`(values) else null
    }
    
    // 불린 조건
    fun booleanEquals(path: BooleanPath?, value: Boolean?): BooleanExpression? {
        return value?.let { path?.eq(it) }
    }
    
    // 조건 결합
    fun and(vararg conditions: BooleanExpression?): BooleanExpression? {
        val nonNullConditions = conditions.filterNotNull()
        return if (nonNullConditions.isEmpty()) {
            null
        } else {
            nonNullConditions.reduce { acc, condition -> acc.and(condition) }
        }
    }
    
    fun or(vararg conditions: BooleanExpression?): BooleanExpression? {
        val nonNullConditions = conditions.filterNotNull()
        return if (nonNullConditions.isEmpty()) {
            null
        } else {
            nonNullConditions.reduce { acc, condition -> acc.or(condition) }
        }
    }
}
```

## Repository 구현 패턴

### 사용자 Repository
```kotlin
interface UserQueryRepository {
    fun findUsersWithFilters(filter: UserSearchFilter, pageable: Pageable): Page<UserEntity>
    fun findActiveUsersWithOrders(): List<UserWithOrdersProjection>
    fun findUserStatistics(startDate: LocalDate, endDate: LocalDate): UserStatistics
    fun findUsersWithRecentActivity(days: Int): List<UserEntity>
}

@Repository
class UserQueryRepositoryImpl(
    queryFactory: JPAQueryFactory
) : BaseQueryRepository(queryFactory), UserQueryRepository {
    
    private val user = QUserEntity.userEntity
    private val order = QOrderEntity.orderEntity
    private val orderItem = QOrderItemEntity.orderItemEntity
    private val conditionBuilder = DynamicConditionBuilder()
    
    override fun findUsersWithFilters(
        filter: UserSearchFilter,
        pageable: Pageable
    ): Page<UserEntity> {
        val query = queryFactory
            .selectFrom(user)
            .where(buildUserConditions(filter))
            .orderBy(*getOrderSpecifiers(pageable.sort, user))
        
        val countQuery = queryFactory
            .select(user.count())
            .from(user)
            .where(buildUserConditions(filter))
        
        return createPage(query, countQuery, pageable)
    }
    
    override fun findActiveUsersWithOrders(): List<UserWithOrdersProjection> {
        return queryFactory
            .select(
                Projections.constructor(
                    UserWithOrdersProjection::class.java,
                    user.id,
                    user.email.value,
                    user.name,
                    order.id.count(),
                    order.totalAmount.amount.sum().coalesce(BigDecimal.ZERO)
                )
            )
            .from(user)
            .leftJoin(order).on(order.userId.eq(user.id))
            .where(user.status.eq(UserStatus.ACTIVE))
            .groupBy(user.id, user.email, user.name)
            .having(order.id.count().gt(0)) // 주문이 있는 사용자만
            .orderBy(order.totalAmount.amount.sum().desc())
            .fetch()
    }
    
    override fun findUserStatistics(
        startDate: LocalDate,
        endDate: LocalDate
    ): UserStatistics {
        val result = queryFactory
            .select(
                user.id.count(),
                user.status.when(UserStatus.ACTIVE).then(1).otherwise(0).sum(),
                user.status.when(UserStatus.INACTIVE).then(1).otherwise(0).sum(),
                user.createdAt.max(),
                user.createdAt.min()
            )
            .from(user)
            .where(
                conditionBuilder.dateBetween(
                    user.createdAt,
                    startDate,
                    endDate
                )
            )
            .fetchOne()
        
        return UserStatistics(
            totalUsers = result?.get(0, Long::class.java) ?: 0L,
            activeUsers = result?.get(1, Long::class.java) ?: 0L,
            inactiveUsers = result?.get(2, Long::class.java) ?: 0L,
            latestSignup = result?.get(3, LocalDateTime::class.java),
            earliestSignup = result?.get(4, LocalDateTime::class.java)
        )
    }
    
    override fun findUsersWithRecentActivity(days: Int): List<UserEntity> {
        val cutoffDate = LocalDateTime.now().minusDays(days.toLong())
        
        // 최근 로그인 또는 주문이 있는 사용자
        val recentLoginUsers = JPAExpressions
            .select(user.id)
            .from(user)
            .where(user.lastLoginAt.after(cutoffDate))
        
        val recentOrderUsers = JPAExpressions
            .select(order.userId)
            .from(order)
            .where(order.createdAt.after(cutoffDate))
        
        return queryFactory
            .selectFrom(user)
            .where(
                user.id.`in`(recentLoginUsers)
                    .or(user.id.`in`(recentOrderUsers))
            )
            .orderBy(user.lastLoginAt.desc().nullsLast())
            .fetch()
    }
    
    private fun buildUserConditions(filter: UserSearchFilter): BooleanExpression? {
        return conditionBuilder.and(
            conditionBuilder.stringContains(user.email.value, filter.email),
            conditionBuilder.stringContains(user.name, filter.name),
            conditionBuilder.enumEquals(user.status, filter.status),
            conditionBuilder.numberBetween(user.age, filter.minAge, filter.maxAge),
            conditionBuilder.dateBetween(user.createdAt, filter.startDate, filter.endDate),
            buildHasOrdersCondition(filter.hasOrders)
        )
    }
    
    private fun buildHasOrdersCondition(hasOrders: Boolean?): BooleanExpression? {
        return hasOrders?.let { has ->
            val orderExistsSubquery = JPAExpressions
                .selectOne()
                .from(order)
                .where(order.userId.eq(user.id))
            
            if (has) orderExistsSubquery.exists()
            else orderExistsSubquery.notExists()
        }
    }
}
```

### 주문 Repository
```kotlin
interface OrderQueryRepository {
    fun findOrdersWithItems(pageable: Pageable): Page<OrderEntity>
    fun findHighValueOrders(minAmount: Money): List<OrderEntity>
    fun getOrderStatistics(period: StatisticsPeriod): OrderStatistics
    fun findOrdersByStatus(statuses: List<OrderStatus>, pageable: Pageable): Page<OrderEntity>
}

@Repository
class OrderQueryRepositoryImpl(
    queryFactory: JPAQueryFactory
) : BaseQueryRepository(queryFactory), OrderQueryRepository {
    
    private val order = QOrderEntity.orderEntity
    private val orderItem = QOrderItemEntity.orderItemEntity
    private val product = QProductEntity.productEntity
    private val user = QUserEntity.userEntity
    private val conditionBuilder = DynamicConditionBuilder()
    
    override fun findOrdersWithItems(pageable: Pageable): Page<OrderEntity> {
        // N+1 문제 방지를 위한 별도 쿼리 전략
        val orderIds = queryFactory
            .select(order.id)
            .from(order)
            .orderBy(*getOrderSpecifiers(pageable.sort, order))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()
        
        if (orderIds.isEmpty()) {
            return PageImpl(emptyList(), pageable, 0)
        }
        
        // 주문들과 아이템들을 배치로 조회
        val orders = queryFactory
            .selectFrom(order)
            .leftJoin(order.items, orderItem).fetchJoin()
            .leftJoin(orderItem.product, product).fetchJoin()
            .where(order.id.`in`(orderIds))
            .orderBy(*getOrderSpecifiers(pageable.sort, order))
            .fetch()
            .distinctBy { it.id } // 중복 제거
        
        val countQuery = queryFactory
            .select(order.count())
            .from(order)
        
        return createPage(
            query = queryFactory.selectFrom(order).where(order.id.`in`(orderIds)),
            countQuery = countQuery,
            pageable = pageable
        ).map { orders.find { order -> order.id == it.id }!! }
    }
    
    override fun findHighValueOrders(minAmount: Money): List<OrderEntity> {
        return queryFactory
            .selectFrom(order)
            .where(
                order.totalAmount.amount.goe(minAmount.amount),
                order.totalAmount.currency.eq(minAmount.currency),
                order.status.eq(OrderStatus.COMPLETED)
            )
            .orderBy(order.totalAmount.amount.desc())
            .limit(100) // 상위 100개만
            .fetch()
    }
    
    override fun getOrderStatistics(period: StatisticsPeriod): OrderStatistics {
        val (startDate, endDate) = period.getDateRange()
        
        val result = queryFactory
            .select(
                order.id.count(),
                order.totalAmount.amount.sum(),
                order.totalAmount.amount.avg(),
                order.totalAmount.amount.max(),
                order.totalAmount.amount.min(),
                order.status.when(OrderStatus.COMPLETED).then(1).otherwise(0).sum(),
                order.status.when(OrderStatus.CANCELLED).then(1).otherwise(0).sum()
            )
            .from(order)
            .where(
                conditionBuilder.dateBetween(order.createdAt, startDate, endDate)
            )
            .fetchOne()
        
        return OrderStatistics(
            totalOrders = result?.get(0, Long::class.java) ?: 0L,
            totalAmount = Money.won(result?.get(1, BigDecimal::class.java) ?: BigDecimal.ZERO),
            averageAmount = Money.won(result?.get(2, BigDecimal::class.java) ?: BigDecimal.ZERO),
            maxAmount = Money.won(result?.get(3, BigDecimal::class.java) ?: BigDecimal.ZERO),
            minAmount = Money.won(result?.get(4, BigDecimal::class.java) ?: BigDecimal.ZERO),
            completedOrders = result?.get(5, Long::class.java) ?: 0L,
            cancelledOrders = result?.get(6, Long::class.java) ?: 0L
        )
    }
    
    override fun findOrdersByStatus(
        statuses: List<OrderStatus>,
        pageable: Pageable
    ): Page<OrderEntity> {
        val query = queryFactory
            .selectFrom(order)
            .where(conditionBuilder.enumIn(order.status, statuses))
            .orderBy(*getOrderSpecifiers(pageable.sort, order))
        
        val countQuery = queryFactory
            .select(order.count())
            .from(order)
            .where(conditionBuilder.enumIn(order.status, statuses))
        
        return createPage(query, countQuery, pageable)
    }
}
```

## 복잡한 쿼리 패턴

### 서브쿼리 활용
```kotlin
@Repository
class AdvancedQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    
    private val user = QUserEntity.userEntity
    private val order = QOrderEntity.orderEntity
    private val orderItem = QOrderItemEntity.orderItemEntity
    private val product = QProductEntity.productEntity
    
    // 고액 구매 고객 찾기 (상관 서브쿼리)
    fun findHighValueCustomers(minTotalAmount: Money): List<UserEntity> {
        return queryFactory
            .selectFrom(user)
            .where(
                JPAExpressions
                    .select(order.totalAmount.amount.sum())
                    .from(order)
                    .where(
                        order.userId.eq(user.id),
                        order.status.eq(OrderStatus.COMPLETED)
                    )
                    .goe(minTotalAmount.amount)
            )
            .fetch()
    }
    
    // 베스트셀러 상품 (윈도우 함수 활용)
    fun findBestSellingProducts(limit: Int): List<ProductSalesInfo> {
        return queryFactory
            .select(
                Projections.constructor(
                    ProductSalesInfo::class.java,
                    product.id,
                    product.name,
                    orderItem.quantity.sum(),
                    orderItem.price.amount.multiply(orderItem.quantity.castToNum(BigDecimal::class.java)).sum(),
                    // RANK() OVER (ORDER BY total_quantity DESC)
                    Expressions.numberTemplate(
                        Long::class.java,
                        "RANK() OVER (ORDER BY SUM({0}) DESC)",
                        orderItem.quantity
                    )
                )
            )
            .from(orderItem)
            .join(orderItem.product, product)
            .join(orderItem.order, order)
            .where(order.status.eq(OrderStatus.COMPLETED))
            .groupBy(product.id, product.name)
            .orderBy(orderItem.quantity.sum().desc())
            .limit(limit.toLong())
            .fetch()
    }
    
    // 월별 매출 트렌드 (EXTRACT 함수 활용)
    fun getMonthlySalesTrend(year: Int): List<MonthlySales> {
        return queryFactory
            .select(
                Projections.constructor(
                    MonthlySales::class.java,
                    Expressions.numberTemplate(
                        Integer::class.java,
                        "EXTRACT(MONTH FROM {0})",
                        order.createdAt
                    ),
                    order.id.count(),
                    order.totalAmount.amount.sum()
                )
            )
            .from(order)
            .where(
                Expressions.numberTemplate(
                    Integer::class.java,
                    "EXTRACT(YEAR FROM {0})",
                    order.createdAt
                ).eq(year),
                order.status.eq(OrderStatus.COMPLETED)
            )
            .groupBy(
                Expressions.numberTemplate(
                    Integer::class.java,
                    "EXTRACT(MONTH FROM {0})",
                    order.createdAt
                )
            )
            .orderBy(
                Expressions.numberTemplate(
                    Integer::class.java,
                    "EXTRACT(MONTH FROM {0})",
                    order.createdAt
                ).asc()
            )
            .fetch()
    }
    
    // CTE (Common Table Expression) 활용
    fun findUsersWithOrderFrequency(): List<UserOrderFrequency> {
        // PostgreSQL의 WITH 절을 네이티브 쿼리로 구현
        return queryFactory
            .select(
                Projections.constructor(
                    UserOrderFrequency::class.java,
                    user.id,
                    user.name,
                    order.id.count(),
                    Expressions.cases()
                        .`when`(order.id.count().gt(10)).then("HIGH")
                        .`when`(order.id.count().between(5, 10)).then("MEDIUM")
                        .otherwise("LOW")
                )
            )
            .from(user)
            .leftJoin(order).on(order.userId.eq(user.id))
            .groupBy(user.id, user.name)
            .orderBy(order.id.count().desc())
            .fetch()
    }
}
```

### 동적 조인과 페치 조인
```kotlin
@Repository
class DynamicJoinRepository(
    private val queryFactory: JPAQueryFactory
) {
    
    private val user = QUserEntity.userEntity
    private val order = QOrderEntity.orderEntity
    private val orderItem = QOrderItemEntity.orderItemEntity
    private val product = QProductEntity.productEntity
    
    fun findUsersWithOptionalJoins(searchCriteria: UserSearchCriteria): List<UserEntity> {
        var query = queryFactory.selectFrom(user)
        
        // 조건에 따라 동적 조인 추가
        if (searchCriteria.includeOrders) {
            query = query.leftJoin(order).on(order.userId.eq(user.id)).fetchJoin()
            
            if (searchCriteria.includeOrderItems) {
                query = query.leftJoin(order.items, orderItem).fetchJoin()
                    .leftJoin(orderItem.product, product).fetchJoin()
            }
        }
        
        // 조건 추가
        val conditions = buildDynamicConditions(searchCriteria)
        if (conditions.isNotEmpty()) {
            query = query.where(*conditions.toTypedArray())
        }
        
        return query
            .distinct() // 중복 제거
            .fetch()
    }
    
    private fun buildDynamicConditions(criteria: UserSearchCriteria): List<BooleanExpression> {
        val conditions = mutableListOf<BooleanExpression>()
        
        criteria.email?.let { email ->
            conditions.add(user.email.value.containsIgnoreCase(email))
        }
        
        criteria.status?.let { status ->
            conditions.add(user.status.eq(status))
        }
        
        if (criteria.includeOrders) {
            criteria.orderStatus?.let { orderStatus ->
                conditions.add(order.status.eq(orderStatus))
            }
            
            criteria.minOrderAmount?.let { amount ->
                conditions.add(order.totalAmount.amount.goe(amount))
            }
        }
        
        return conditions
    }
}
```

## 성능 최적화 패턴

### N+1 문제 해결
```kotlin
@Repository
class OptimizedQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    
    private val order = QOrderEntity.orderEntity
    private val orderItem = QOrderItemEntity.orderItemEntity
    private val product = QProductEntity.productEntity
    
    // ❌ N+1 문제 발생 가능
    fun findOrdersNaive(): List<OrderEntity> {
        return queryFactory
            .selectFrom(order)
            .fetch()
        // 각 주문의 items에 접근할 때마다 추가 쿼리 발생
    }
    
    // ✅ Batch Fetch로 해결
    fun findOrdersOptimizedBatch(orderIds: List<Long>): Map<Long, List<OrderItemEntity>> {
        return queryFactory
            .selectFrom(orderItem)
            .join(orderItem.product, product).fetchJoin()
            .where(orderItem.order.id.`in`(orderIds))
            .fetch()
            .groupBy { it.order.id }
    }
    
    // ✅ Projection으로 필요한 데이터만 조회
    fun findOrderSummaries(): List<OrderSummary> {
        return queryFactory
            .select(
                Projections.constructor(
                    OrderSummary::class.java,
                    order.id,
                    order.totalAmount,
                    order.status,
                    order.createdAt,
                    orderItem.id.count()
                )
            )
            .from(order)
            .leftJoin(order.items, orderItem)
            .groupBy(order.id, order.totalAmount, order.status, order.createdAt)
            .fetch()
    }
    
    // ✅ 두 단계 쿼리로 최적화
    fun findOrdersWithItemsOptimized(limit: Int): List<OrderEntity> {
        // 1단계: 주문 ID만 조회
        val orderIds = queryFactory
            .select(order.id)
            .from(order)
            .orderBy(order.createdAt.desc())
            .limit(limit.toLong())
            .fetch()
        
        if (orderIds.isEmpty()) {
            return emptyList()
        }
        
        // 2단계: 주문과 아이템을 배치로 조회
        return queryFactory
            .selectFrom(order)
            .leftJoin(order.items, orderItem).fetchJoin()
            .leftJoin(orderItem.product, product).fetchJoin()
            .where(order.id.`in`(orderIds))
            .orderBy(order.createdAt.desc())
            .fetch()
            .distinctBy { it.id }
    }
}
```

### 커서 기반 페이징
```kotlin
@Repository
class CursorPagingRepository(
    private val queryFactory: JPAQueryFactory
) {
    
    private val user = QUserEntity.userEntity
    
    data class CursorPage<T>(
        val content: List<T>,
        val nextCursor: String?,
        val hasNext: Boolean,
        val size: Int
    )
    
    fun findUsersWithCursorPaging(
        cursor: String? = null,
        size: Int = 20
    ): CursorPage<UserEntity> {
        var query = queryFactory
            .selectFrom(user)
            .where(user.status.eq(UserStatus.ACTIVE))
        
        // 커서가 있으면 해당 위치 이후부터 조회
        cursor?.let { cursorValue ->
            val (id, createdAt) = parseCursor(cursorValue)
            query = query.where(
                user.createdAt.lt(createdAt)
                    .or(user.createdAt.eq(createdAt).and(user.id.gt(id)))
            )
        }
        
        val users = query
            .orderBy(user.createdAt.desc(), user.id.asc())
            .limit((size + 1).toLong()) // 다음 페이지 존재 여부 확인용
            .fetch()
        
        val hasNext = users.size > size
        val content = if (hasNext) users.dropLast(1) else users
        val nextCursor = if (hasNext) {
            val lastUser = content.last()
            createCursor(lastUser.id, lastUser.createdAt)
        } else null
        
        return CursorPage(content, nextCursor, hasNext, content.size)
    }
    
    private fun parseCursor(cursor: String): Pair<Long, LocalDateTime> {
        val decoded = String(Base64.getDecoder().decode(cursor))
        val parts = decoded.split("|")
        return Pair(
            parts[0].toLong(),
            LocalDateTime.parse(parts[1])
        )
    }
    
    private fun createCursor(id: Long, createdAt: LocalDateTime): String {
        val cursorString = "$id|$createdAt"
        return Base64.getEncoder().encodeToString(cursorString.toByteArray())
    }
}
```

### 캐시 활용 최적화
```kotlin
@Repository
class CachedQueryRepository(
    private val queryFactory: JPAQueryFactory,
    private val redisTemplate: RedisTemplate<String, Any>
) {
    
    private val user = QUserEntity.userEntity
    
    @Cacheable(value = ["user-statistics"], key = "#period.name")
    fun getUserStatistics(period: StatisticsPeriod): UserStatistics {
        val (startDate, endDate) = period.getDateRange()
        
        return queryFactory
            .select(
                Projections.constructor(
                    UserStatistics::class.java,
                    user.id.count(),
                    user.status.when(UserStatus.ACTIVE).then(1).otherwise(0).sum(),
                    user.createdAt.max(),
                    user.createdAt.min()
                )
            )
            .from(user)
            .where(user.createdAt.between(startDate.atStartOfDay(), endDate.atTime(23, 59, 59)))
            .fetchOne() ?: UserStatistics()
    }
    
    fun findUserWithCache(userId: Long): UserEntity? {
        val cacheKey = "user:$userId"
        
        // Redis에서 조회
        val cachedUser = redisTemplate.opsForValue().get(cacheKey) as UserEntity?
        if (cachedUser != null) {
            return cachedUser
        }
        
        // DB에서 조회
        val user = queryFactory
            .selectFrom(user)
            .where(user.id.eq(userId))
            .fetchOne()
        
        // Redis에 캐시
        user?.let {
            redisTemplate.opsForValue().set(cacheKey, it, Duration.ofMinutes(30))
        }
        
        return user
    }
    
    @CacheEvict(value = ["user-statistics"], allEntries = true)
    fun evictUserStatisticsCache() {
        // 사용자 통계 캐시 무효화
    }
}
```

## 동적 쿼리 빌더 고급 패턴

### 플루언트 API 스타일 빌더
```kotlin
class UserQueryBuilder(private val queryFactory: JPAQueryFactory) {
    
    private val user = QUserEntity.userEntity
    private val order = QOrderEntity.orderEntity
    private var query = queryFactory.selectFrom(user)
    private val conditions = mutableListOf<BooleanExpression>()
    
    fun withEmail(email: String): UserQueryBuilder {
        if (email.isNotBlank()) {
            conditions.add(user.email.value.containsIgnoreCase(email))
        }
        return this
    }
    
    fun withStatus(status: UserStatus): UserQueryBuilder {
        conditions.add(user.status.eq(status))
        return this
    }
    
    fun withAgeRange(min: Int, max: Int): UserQueryBuilder {
        conditions.add(user.age.between(min, max))
        return this
    }
    
    fun createdAfter(date: LocalDateTime): UserQueryBuilder {
        conditions.add(user.createdAt.after(date))
        return this
    }
    
    fun hasOrdersInStatus(orderStatus: OrderStatus): UserQueryBuilder {
        val orderExistsSubquery = JPAExpressions
            .selectOne()
            .from(order)
            .where(
                order.userId.eq(user.id),
                order.status.eq(orderStatus)
            )
        
        conditions.add(orderExistsSubquery.exists())
        return this
    }
    
    fun orderBy(sort: Sort): UserQueryBuilder {
        val orderSpecifiers = sort.map { order ->
            val path = when (order.property) {
                "email" -> user.email.value
                "name" -> user.name
                "createdAt" -> user.createdAt
                "age" -> user.age
                else -> user.id
            }
            
            if (order.isAscending) {
                OrderSpecifier(Order.ASC, path)
            } else {
                OrderSpecifier(Order.DESC, path)
            }
        }.toTypedArray()
        
        query = query.orderBy(*orderSpecifiers)
        return this
    }
    
    fun limit(limit: Long): UserQueryBuilder {
        query = query.limit(limit)
        return this
    }
    
    fun build(): List<UserEntity> {
        if (conditions.isNotEmpty()) {
            val combinedCondition = conditions.reduce { acc, condition -> acc.and(condition) }
            query = query.where(combinedCondition)
        }
        
        return query.fetch()
    }
    
    fun buildPage(pageable: Pageable): Page<UserEntity> {
        val combinedCondition = if (conditions.isNotEmpty()) {
            conditions.reduce { acc, condition -> acc.and(condition) }
        } else null
        
        val dataQuery = queryFactory
            .selectFrom(user)
            .where(combinedCondition)
            .orderBy(*getOrderSpecifiers(pageable.sort))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
        
        val countQuery = queryFactory
            .select(user.count())
            .from(user)
            .where(combinedCondition)
        
        val results = dataQuery.fetch()
        val total = countQuery.fetchOne() ?: 0L
        
        return PageImpl(results, pageable, total)
    }
    
    private fun getOrderSpecifiers(sort: Sort): Array<OrderSpecifier<*>> {
        return sort.map { order ->
            val path = when (order.property) {
                "email" -> user.email.value
                "name" -> user.name
                "createdAt" -> user.createdAt
                "age" -> user.age
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

// 사용 예시
fun findUsers(searchCriteria: UserSearchCriteria, pageable: Pageable): Page<UserEntity> {
    return UserQueryBuilder(queryFactory)
        .withEmail(searchCriteria.email)
        .withStatus(UserStatus.ACTIVE)
        .withAgeRange(18, 65)
        .hasOrdersInStatus(OrderStatus.COMPLETED)
        .buildPage(pageable)
}
```

이러한 QueryDSL 패턴들을 활용하면 타입 안전하고 효율적인 데이터베이스 쿼리를 작성할 수 있습니다. 각 패턴은 특정 상황에 최적화되어 있으며, 프로젝트의 요구사항에 맞게 조합하여 사용할 수 있습니다.