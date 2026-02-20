---
skill: 테스트 패턴 라이브러리
category: Testing
patterns: BehaviorSpec, MockK, TestContainers, Fixture
---

# Testing Patterns Skill

Kotest, MockK, TestContainers를 활용한 포괄적 테스트 패턴 라이브러리입니다.

## 테스트 베이스 클래스

### 단위 테스트 베이스
```kotlin
abstract class UnitTestBase : BehaviorSpec() {
    
    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf
    
    companion object {
        val fixture = kotlinFixture {
            // 기본 픽스쳐 설정
            property(User::id) { Random.nextLong(1, 10000) }
            property(User::createdAt) { LocalDateTime.now().minusDays(Random.nextLong(0, 365)) }
            property(User::status) { UserStatus.ACTIVE }
            
            // 이메일 픽스쳐
            property(User::email) { "test${Random.nextInt(1000)}@example.com" }
        }
    }
    
    // 공통 매처 및 헬퍼 메서드
    protected fun <T> mockBean(): T = mockk()
    protected fun <T> relaxedMock(): T = mockk(relaxed = true)
    protected fun <T> spyBean(obj: T): T = spyk(obj)
}
```

### JPA 통합 테스트 베이스
```kotlin
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
abstract class BaseJpaBehaviorSpec(
    body: BehaviorSpec.() -> Unit = {}
) : BehaviorSpec(body) {
    
    @Autowired
    protected lateinit var testEntityManager: TestEntityManager
    
    override fun isolationMode() = IsolationMode.InstancePerLeaf
    
    companion object {
        val fixture = kotlinFixture()
    }
    
    protected fun <T> persist(entity: T): T {
        testEntityManager.persistAndFlush(entity)
        return entity
    }
    
    protected fun <T> persistAndClear(entity: T): T {
        testEntityManager.persistAndFlush(entity)
        testEntityManager.clear()
        return entity
    }
    
    protected fun clearDatabase() {
        testEntityManager.clear()
    }
    
    protected fun flushAndClear() {
        testEntityManager.flush()
        testEntityManager.clear()
    }
    
    // 트랜잭션 경계 테스트를 위한 헬퍼
    protected fun <T> executeInNewTransaction(action: () -> T): T {
        return TransactionTemplate(testEntityManager.entityManager
            .unwrap(Session::class.java)
            .sessionFactory.currentSession.transaction.transactionCoordinator
            .transactionCoordinatorBuilder.buildTransactionCoordinator()
        ).execute { action() } ?: throw IllegalStateException("Transaction execution failed")
    }
}
```

### 웹 레이어 테스트 베이스
```kotlin
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseWebLayerTest(
    body: BehaviorSpec.() -> Unit = {}
) : BehaviorSpec(body) {
    
    @Autowired
    protected lateinit var mockMvc: MockMvc
    
    @Autowired
    protected lateinit var objectMapper: ObjectMapper
    
    // JSON 파싱 헬퍼
    protected inline fun <reified T> String.parseJson(): T = 
        objectMapper.readValue(this, T::class.java)
    
    protected fun Any.toJson(): String = 
        objectMapper.writeValueAsString(this)
    
    // MockMvc 확장 함수
    protected fun MockMvc.postJson(url: String, content: Any): ResultActions =
        post(url) {
            contentType = MediaType.APPLICATION_JSON
            this.content = content.toJson()
        }
    
    protected fun MockMvc.putJson(url: String, content: Any): ResultActions =
        put(url) {
            contentType = MediaType.APPLICATION_JSON
            this.content = content.toJson()
        }
    
    protected fun MockMvc.patchJson(url: String, content: Any): ResultActions =
        patch(url) {
            contentType = MediaType.APPLICATION_JSON
            this.content = content.toJson()
        }
}
```

## 테스트 데이터 빌더

### 범용 엔티티 빌더
```kotlin
class TestEntityBuilder<T : Any>(
    private val clazz: KClass<T>,
    private val fixture: Fixture
) {
    private val customizations = mutableMapOf<KProperty1<T, *>, Any?>()
    
    fun <V> with(property: KProperty1<T, V>, value: V): TestEntityBuilder<T> {
        customizations[property] = value
        return this
    }
    
    fun <V> with(property: KProperty1<T, V>, valueSupplier: () -> V): TestEntityBuilder<T> {
        customizations[property] = valueSupplier()
        return this
    }
    
    fun build(): T {
        val baseEntity = fixture.create<T>()
        
        customizations.forEach { (property, value) ->
            @Suppress("UNCHECKED_CAST")
            (property as KMutableProperty1<T, Any?>).set(baseEntity, value)
        }
        
        return baseEntity
    }
    
    fun buildList(count: Int): List<T> = (1..count).map { build() }
}

// DSL 스타일 빌더
inline fun <reified T : Any> testEntity(
    fixture: Fixture = kotlinFixture(),
    builder: TestEntityBuilder<T>.() -> Unit = {}
): T {
    return TestEntityBuilder(T::class, fixture).apply(builder).build()
}

inline fun <reified T : Any> testEntityList(
    count: Int,
    fixture: Fixture = kotlinFixture(),
    builder: TestEntityBuilder<T>.() -> Unit = {}
): List<T> {
    return TestEntityBuilder(T::class, fixture).apply(builder).buildList(count)
}
```

### 특화된 엔티티 생성 함수
```kotlin
// User 엔티티 생성
fun createUserMockEntity(
    fixture: Fixture = kotlinFixture(),
    id: Long? = null,
    email: String? = null,
    name: String = "테스트 사용자",
    status: UserStatus = UserStatus.ACTIVE,
    createdAt: LocalDateTime = LocalDateTime.now(),
    profile: UserProfile = UserProfile()
): UserEntity = fixture.create<UserEntity> {
    property(UserEntity::id) { id ?: Random.nextLong(1, 10000) }
    property(UserEntity::email) { Email.of(email ?: "user${Random.nextInt(1000)}@test.com") }
    property(UserEntity::name) { name }
    property(UserEntity::status) { status }
    property(UserEntity::createdAt) { createdAt }
    property(UserEntity::profile) { profile }
}

// Order 엔티티 생성
fun createOrderMockEntity(
    fixture: Fixture = kotlinFixture(),
    id: Long? = null,
    userId: Long,
    status: OrderStatus = OrderStatus.PENDING,
    totalAmount: Money = Money.won(10000),
    items: List<OrderItemEntity> = emptyList()
): OrderEntity = fixture.create<OrderEntity> {
    property(OrderEntity::id) { id ?: Random.nextLong(1, 10000) }
    property(OrderEntity::userId) { userId }
    property(OrderEntity::status) { status }
    property(OrderEntity::totalAmount) { totalAmount }
    property(OrderEntity::items) { items.toMutableList() }
}

// Product 엔티티 생성
fun createProductMockEntity(
    fixture: Fixture = kotlinFixture(),
    id: Long? = null,
    name: String = "테스트 상품",
    price: Money = Money.won(5000),
    stock: Int = 100,
    category: ProductCategory = ProductCategory.GENERAL
): ProductEntity = fixture.create<ProductEntity> {
    property(ProductEntity::id) { id ?: Random.nextLong(1, 10000) }
    property(ProductEntity::name) { name }
    property(ProductEntity::price) { price }
    property(ProductEntity::stock) { stock }
    property(ProductEntity::category) { category }
}
```

## Mock 설정 패턴

### MockK 확장 및 헬퍼
```kotlin
// Mock 상태 검증 헬퍼
object MockVerification {
    
    fun <T> verifyOnce(mock: T, action: T.() -> Any) {
        verify(exactly = 1) { mock.action() }
    }
    
    fun <T> verifyNever(mock: T, action: T.() -> Any) {
        verify(exactly = 0) { mock.action() }
    }
    
    fun <T> verifyAtLeast(times: Int, mock: T, action: T.() -> Any) {
        verify(atLeast = times) { mock.action() }
    }
    
    fun <T> verifyInOrder(mock: T, vararg actions: T.() -> Any) {
        verifyOrder {
            actions.forEach { mock.it() }
        }
    }
}

// Mock 응답 설정 헬퍼
object MockResponses {
    
    fun <T> T.returnsSuccess(result: Any) {
        every { this@returnsSuccess } returns result
    }
    
    fun <T> T.throwsException(exception: Exception) {
        every { this@throwsException } throws exception
    }
    
    fun <T> T.returnsSequence(vararg results: Any) {
        every { this@returnsSequence } returnsMany results.toList()
    }
    
    fun <T> T.answersWithDelay(delayMs: Long, result: Any) {
        every { this@answersWithDelay } answers {
            Thread.sleep(delayMs)
            result
        }
    }
}

// 코루틴 Mock 헬퍼
object CoroutineMocks {
    
    fun <T> mockSuspendingFunction(result: T): suspend () -> T = mockk {
        coEvery { this@mockk.invoke() } returns result
    }
    
    fun <T> mockSuspendingFunctionWithDelay(result: T, delayMs: Long): suspend () -> T = mockk {
        coEvery { this@mockk.invoke() } coAnswers {
            delay(delayMs)
            result
        }
    }
    
    fun mockSuspendingFunctionWithException(exception: Exception): suspend () -> Unit = mockk {
        coEvery { this@mockk.invoke() } throws exception
    }
}
```

### Repository Mock 패턴
```kotlin
class UserRepositoryMock {
    val mock: UserRepository = mockk()
    private val users = mutableMapOf<Long, UserEntity>()
    
    fun setupEmptyRepository() {
        every { mock.findById(any()) } returns null
        every { mock.existsById(any()) } returns false
        every { mock.findByEmail(any()) } returns null
        every { mock.existsByEmail(any()) } returns false
        every { mock.findAll() } returns emptyList()
        every { mock.count() } returns 0
    }
    
    fun addUser(user: UserEntity): UserRepositoryMock {
        users[user.id] = user
        updateMockBehavior()
        return this
    }
    
    fun addUsers(userList: List<UserEntity>): UserRepositoryMock {
        userList.forEach { users[it.id] = it }
        updateMockBehavior()
        return this
    }
    
    fun userExists(id: Long): UserRepositoryMock {
        every { mock.existsById(id) } returns true
        return this
    }
    
    fun userNotExists(id: Long): UserRepositoryMock {
        every { mock.existsById(id) } returns false
        return this
    }
    
    private fun updateMockBehavior() {
        // findById 설정
        every { mock.findById(any()) } answers {
            users[firstArg()]
        }
        
        // existsById 설정
        every { mock.existsById(any()) } answers {
            users.containsKey(firstArg())
        }
        
        // findByEmail 설정
        every { mock.findByEmail(any()) } answers {
            users.values.find { it.email.value == firstArg<Email>().value }
        }
        
        // existsByEmail 설정
        every { mock.existsByEmail(any()) } answers {
            users.values.any { it.email.value == firstArg<Email>().value }
        }
        
        // save 설정
        every { mock.save(any()) } answers {
            val user = firstArg<UserEntity>()
            users[user.id] = user
            user
        }
        
        // findAll 설정
        every { mock.findAll() } returns users.values.toList()
        
        // count 설정
        every { mock.count() } returns users.size.toLong()
    }
}
```

## 테스트 시나리오 패턴

### Given-When-Then 헬퍼
```kotlin
class TestScenario<T> {
    private var givenState: (() -> T)? = null
    private var whenAction: ((T) -> Any)? = null
    private var thenVerifications: MutableList<(T, Any) -> Unit> = mutableListOf()
    
    fun given(setup: () -> T): TestScenario<T> {
        givenState = setup
        return this
    }
    
    fun <R> whenAction(action: (T) -> R): TestScenario<T> {
        @Suppress("UNCHECKED_CAST")
        whenAction = action as (T) -> Any
        return this
    }
    
    fun then(verification: (T, Any) -> Unit): TestScenario<T> {
        thenVerifications.add(verification)
        return this
    }
    
    fun execute() {
        val state = givenState?.invoke() ?: throw IllegalStateException("Given state not defined")
        val result = whenAction?.invoke(state) ?: throw IllegalStateException("When action not defined")
        
        thenVerifications.forEach { verification ->
            verification(state, result)
        }
    }
}

// DSL 스타일 사용을 위한 함수
fun <T> scenario(block: TestScenario<T>.() -> Unit) {
    TestScenario<T>().apply(block).execute()
}
```

### 비동기 테스트 패턴
```kotlin
class AsyncTestHelper {
    
    companion object {
        // 코루틴 테스트 실행
        fun runAsyncTest(
            testDispatcher: TestDispatcher = StandardTestDispatcher(),
            block: suspend TestScope.() -> Unit
        ) = runTest(testDispatcher) { block() }
        
        // 가상 시간 진행
        suspend fun TestScope.advanceTimeAndRun(delayTimeMillis: Long) {
            testScheduler.advanceTimeBy(delayTimeMillis)
            testScheduler.runCurrent()
        }
        
        // Flow 테스트
        suspend fun <T> Flow<T>.test(
            testScope: TestScope,
            assertions: suspend FlowCollector<T>.() -> Unit
        ) {
            val collector = TestFlowCollector<T>()
            testScope.launch {
                collect(collector)
            }
            collector.assertions()
            testScope.testScheduler.advanceUntilIdle()
        }
    }
    
    private class TestFlowCollector<T> : FlowCollector<T> {
        private val values = mutableListOf<T>()
        private var error: Throwable? = null
        private var completed = false
        
        override suspend fun emit(value: T) {
            values.add(value)
        }
        
        fun emitError(error: Throwable) {
            this.error = error
        }
        
        fun complete() {
            completed = true
        }
        
        val emittedValues: List<T> get() = values.toList()
        val hasError: Boolean get() = error != null
        val isCompleted: Boolean get() = completed
        
        fun assertEmittedValues(expected: List<T>) {
            values shouldBe expected
        }
        
        fun assertEmittedCount(count: Int) {
            values.size shouldBe count
        }
        
        fun assertNoErrors() {
            error shouldBe null
        }
        
        fun assertError(expectedError: KClass<out Throwable>) {
            error shouldNotBe null
            error!!::class shouldBe expectedError
        }
    }
}
```

## 데이터 검증 헬퍼

### 커스텀 매처
```kotlin
// Entity 검증 매처
fun UserEntity.shouldHaveValidProfile() {
    profile.shouldNotBeNull()
    profile.email?.isValidEmail() shouldBe true
}

fun List<UserEntity>.shouldAllBeActive() {
    all { it.status == UserStatus.ACTIVE } shouldBe true
}

fun OrderEntity.shouldHaveValidTotalAmount() {
    totalAmount.isPositive() shouldBe true
    totalAmount shouldBe items.sumOf { it.price * Money.of(it.quantity) }
}

// 날짜 관련 매처
infix fun LocalDateTime.shouldBeCloseTo(other: LocalDateTime) {
    val duration = Duration.between(this, other)
    duration.abs() shouldBeLessThan Duration.ofSeconds(1)
}

infix fun LocalDate.shouldBeBetween(range: Pair<LocalDate, LocalDate>) {
    this shouldBeGreaterThanOrEqualTo range.first
    this shouldBeLessThanOrEqualTo range.second
}

// 컬렉션 매처
infix fun <T> Collection<T>.shouldContainExactlyInAnyOrder(expected: Collection<T>) {
    this should containExactlyInAnyOrder(expected)
}

fun <T> Collection<T>.shouldHaveUniqueElements() {
    this.size shouldBe this.distinct().size
}

// Money 매처  
infix fun Money.shouldBeGreaterThan(other: Money) {
    this.isGreaterThan(other) shouldBe true
}

infix fun Money.shouldEqual(other: Money) {
    this.amount shouldBe other.amount
    this.currency shouldBe other.currency
}
```

### 데이터 검증 DSL
```kotlin
class EntityAssertion<T>(private val entity: T) {
    
    fun hasField(fieldName: String, expectedValue: Any): EntityAssertion<T> {
        val field = entity!!::class.memberProperties.find { it.name == fieldName }
        field shouldNotBe null
        field!!.call(entity) shouldBe expectedValue
        return this
    }
    
    fun satisfies(condition: (T) -> Boolean, message: String = ""): EntityAssertion<T> {
        condition(entity) shouldBe true
        return this
    }
    
    fun hasProperty(property: KProperty1<T, *>, value: Any): EntityAssertion<T> {
        property.get(entity) shouldBe value
        return this
    }
}

fun <T> T.shouldSatisfy(block: EntityAssertion<T>.() -> Unit) {
    EntityAssertion(this).apply(block)
}

// 사용 예시
user.shouldSatisfy {
    hasProperty(UserEntity::email, expectedEmail)
    hasProperty(UserEntity::status, UserStatus.ACTIVE)
    satisfies({ it.createdAt.isBefore(LocalDateTime.now()) }, "Created time should be in the past")
}
```

## 성능 테스트 패턴

### 벤치마크 테스트
```kotlin
class PerformanceTestHelper {
    
    data class PerformanceResult(
        val averageTimeMs: Double,
        val minTimeMs: Long,
        val maxTimeMs: Long,
        val totalIterations: Int
    )
    
    fun measurePerformance(
        iterations: Int = 100,
        warmupRuns: Int = 10,
        operation: () -> Unit
    ): PerformanceResult {
        // 웜업
        repeat(warmupRuns) { operation() }
        
        val times = mutableListOf<Long>()
        
        repeat(iterations) {
            val startTime = System.nanoTime()
            operation()
            val endTime = System.nanoTime()
            times.add((endTime - startTime) / 1_000_000) // 밀리초로 변환
        }
        
        return PerformanceResult(
            averageTimeMs = times.average(),
            minTimeMs = times.minOrNull() ?: 0,
            maxTimeMs = times.maxOrNull() ?: 0,
            totalIterations = iterations
        )
    }
    
    fun measureAsyncPerformance(
        iterations: Int = 100,
        operation: suspend () -> Unit
    ): PerformanceResult = runBlocking {
        val times = mutableListOf<Long>()
        
        repeat(iterations) {
            val startTime = System.nanoTime()
            operation()
            val endTime = System.nanoTime()
            times.add((endTime - startTime) / 1_000_000)
        }
        
        PerformanceResult(
            averageTimeMs = times.average(),
            minTimeMs = times.minOrNull() ?: 0,
            maxTimeMs = times.maxOrNull() ?: 0,
            totalIterations = iterations
        )
    }
}

// 성능 검증 매처
infix fun PerformanceTestHelper.PerformanceResult.shouldExecuteWithin(maxTimeMs: Long) {
    averageTimeMs should beLessThan(maxTimeMs.toDouble())
}

fun PerformanceTestHelper.PerformanceResult.shouldBeConsistent(maxDeviationPercent: Double = 20.0) {
    val deviation = ((maxTimeMs - minTimeMs).toDouble() / averageTimeMs) * 100
    deviation should beLessThan(maxDeviationPercent)
}
```

## 통합 테스트 설정

### TestContainers 설정
```kotlin
@SpringBootTest
@Testcontainers
class DatabaseIntegrationTestBase : BehaviorSpec() {
    
    companion object {
        @JvmStatic
        @Container
        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:15-alpine")
            .apply {
                withDatabaseName("testdb")
                withUsername("testuser")
                withPassword("testpass")
                withReuse(true) // 테스트 간 재사용
            }
        
        @JvmStatic
        @Container
        val redisContainer = GenericContainer<Nothing>("redis:7-alpine")
            .apply {
                withExposedPorts(6379)
                withReuse(true)
            }
        
        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            // PostgreSQL 설정
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
            
            // Redis 설정
            registry.add("spring.data.redis.host", redisContainer::getHost)
            registry.add("spring.data.redis.port") { redisContainer.getMappedPort(6379) }
        }
    }
}
```

### 테스트 프로파일 설정
```kotlin
@TestConfiguration
class TestConfig {
    
    @Bean
    @Primary
    fun testTimeProvider(): TimeProvider = FixedTimeProvider(
        LocalDateTime.of(2024, 1, 1, 12, 0)
    )
    
    @Bean
    @Primary
    fun testEmailService(): EmailService = mockk<EmailService>().apply {
        every { sendEmail(any(), any(), any()) } just Runs
    }
    
    @Bean
    @Primary  
    fun testNotificationService(): NotificationService = mockk<NotificationService>().apply {
        every { sendNotification(any()) } just Runs
    }
}

class FixedTimeProvider(private val fixedTime: LocalDateTime) : TimeProvider {
    override fun now(): LocalDateTime = fixedTime
    override fun today(): LocalDate = fixedTime.toLocalDate()
}
```

이러한 테스트 패턴들을 사용하여 일관성 있고 효율적인 테스트 코드를 작성할 수 있습니다. 각 패턴은 특정 테스트 시나리오에 최적화되어 있으며, 재사용 가능한 컴포넌트로 설계되었습니다.