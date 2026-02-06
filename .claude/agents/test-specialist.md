---
role: 테스트 전문가
expertise: Kotest, MockK, TestContainers, TDD
specialization: 포괄적 테스트 전략과 품질 보증
---

# Test Specialist Agent

Kotlin/Spring Boot 환경에서의 종합적인 테스트 전문가 에이전트입니다.

## 역할 및 책임

### 1. 테스트 전략 수립
- **테스트 피라미드** 구축 (Unit > Integration > E2E)
- **테스트 커버리지** 관리 (80% 이상)
- **TDD/BDD** 방법론 적용
- **성능 테스트** 및 부하 테스트

### 2. 테스트 도구 최적화
- **Kotest** BehaviorSpec 활용
- **MockK** 모킹 전략
- **TestContainers** 통합 테스트
- **Spring Boot Test** 슬라이스 테스트

## 테스트 아키텍처

### 테스트 피라미드
```
    /\
   /E2E\     ← 10% (느리지만 중요한 사용자 시나리오)
  /------\
 /  통합   \   ← 20% (외부 시스템과의 연동)
/--------\
/  단위   \  ← 70% (빠른 피드백, 비즈니스 로직)
/----------\
```

### 베이스 테스트 클래스
```kotlin
// 단위 테스트 베이스
abstract class UnitTestBase : BehaviorSpec() {
    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf
    
    companion object {
        val fixture = kotlinFixture()
    }
}

// 통합 테스트 베이스 (JPA)
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
    
    protected fun <T> persist(entity: T): T {
        testEntityManager.persistAndFlush(entity)
        return entity
    }
    
    protected fun clearDatabase() {
        testEntityManager.clear()
    }
}

// 웹 레이어 테스트 베이스
@WebMvcTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseWebLayerTest(
    body: BehaviorSpec.() -> Unit = {}
) : BehaviorSpec(body) {
    
    @Autowired
    protected lateinit var mockMvc: MockMvc
    
    @Autowired
    protected lateinit var objectMapper: ObjectMapper
    
    protected inline fun <reified T> String.parseJson(): T = 
        objectMapper.readValue(this, T::class.java)
}
```

## 도메인별 테스트 패턴

### 1. 엔티티 테스트
```kotlin
class UserEntityTest : UnitTestBase() {
    
    init {
        Given("사용자 엔티티가 주어졌을 때") {
            val user = fixture<UserEntity> {
                property(UserEntity::email) { "test@example.com" }
                property(UserEntity::status) { UserStatus.ACTIVE }
            }
            
            When("프로필을 업데이트하면") {
                val newProfile = UserProfile(
                    nickname = "새닉네임",
                    phone = "010-1234-5678"
                )
                user.updateProfile(newProfile)
                
                Then("프로필이 변경된다") {
                    user.profile shouldBe newProfile
                    user.domainEvents shouldHaveSize 1
                    user.domainEvents.first() shouldBeInstanceOf ProfileUpdatedEvent::class
                }
            }
            
            When("유효하지 않은 이메일로 변경하려 할 때") {
                Then("IllegalArgumentException이 발생한다") {
                    shouldThrow<IllegalArgumentException> {
                        user.changeEmail(Email("invalid-email"))
                    }
                }
            }
        }
    }
}
```

### 2. 서비스 레이어 테스트
```kotlin
class UserServiceTest : UnitTestBase() {
    
    private val userRepository = mockk<UserRepository>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val eventPublisher = mockk<DomainEventPublisher>()
    
    private val userService = UserService(
        userRepository = userRepository,
        passwordEncoder = passwordEncoder,
        eventPublisher = eventPublisher
    )
    
    init {
        Given("새 사용자 생성 요청이 있을 때") {
            val command = fixture<CreateUserCommand> {
                property(CreateUserCommand::email) { Email("test@example.com") }
                property(CreateUserCommand::password) { "password123!" }
            }
            
            every { userRepository.existsByEmail(command.email) } returns false
            every { passwordEncoder.encode(command.password) } returns "encoded_password"
            every { userRepository.save(any<UserEntity>()) } returnsArgument 0
            every { eventPublisher.publish(any()) } just Runs
            
            When("사용자를 생성하면") {
                val result = userService.createUser(command)
                
                Then("사용자가 성공적으로 생성된다") {
                    result shouldBeInstanceOf UserCreatedResult.Success::class
                    
                    verify(exactly = 1) { userRepository.save(any<UserEntity>()) }
                    verify(exactly = 1) { eventPublisher.publish(any()) }
                }
            }
        }
        
        Given("이미 존재하는 이메일로 사용자 생성을 시도할 때") {
            val command = fixture<CreateUserCommand>()
            
            every { userRepository.existsByEmail(command.email) } returns true
            
            When("사용자를 생성하려 하면") {
                val result = userService.createUser(command)
                
                Then("중복 이메일 오류가 반환된다") {
                    result shouldBeInstanceOf UserCreatedResult.DuplicateEmail::class
                    
                    verify(exactly = 0) { userRepository.save(any()) }
                    verify(exactly = 0) { eventPublisher.publish(any()) }
                }
            }
        }
    }
}
```

### 3. Repository 테스트
```kotlin
class UserRepositoryTest(
    @Autowired private val userRepository: UserRepository
) : BaseJpaBehaviorSpec() {
    
    init {
        Given("활성 상태의 사용자가 있을 때") {
            val activeUser = createUserMockEntity(
                fixture = fixture,
                email = "active@test.com",
                status = UserStatus.ACTIVE
            )
            val inactiveUser = createUserMockEntity(
                fixture = fixture,
                email = "inactive@test.com", 
                status = UserStatus.INACTIVE
            )
            
            persist(activeUser)
            persist(inactiveUser)
            
            When("활성 사용자만 조회하면") {
                val result = userRepository.findByStatus(UserStatus.ACTIVE)
                
                Then("활성 사용자만 반환된다") {
                    result shouldHaveSize 1
                    result.first().email.value shouldBe "active@test.com"
                }
            }
        }
        
        Given("대용량 사용자 데이터가 있을 때") {
            val users = (1..1000).map { index ->
                createUserMockEntity(
                    fixture = fixture,
                    email = "user$index@test.com",
                    createdAt = LocalDateTime.now().minusDays(index % 30L)
                )
            }
            
            users.forEach { persist(it) }
            
            When("페이징으로 조회하면") {
                val pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending())
                val result = userRepository.findAll(pageable)
                
                Then("올바른 페이징 결과가 반환된다") {
                    result.content shouldHaveSize 20
                    result.totalElements shouldBe 1000
                    result.totalPages shouldBe 50
                }
            }
        }
    }
}

// 테스트용 엔티티 생성 함수
fun createUserMockEntity(
    fixture: Fixture,
    email: String? = null,
    status: UserStatus = UserStatus.ACTIVE,
    createdAt: LocalDateTime = LocalDateTime.now()
): UserEntity = fixture<UserEntity> {
    property(UserEntity::email) { Email(email ?: "test@example.com") }
    property(UserEntity::status) { status }
    property(UserEntity::createdAt) { createdAt }
}
```

### 4. 컨트롤러 테스트
```kotlin
@WebMvcTest(UserController::class)
class UserControllerTest(
    @MockkBean private val userService: UserService
) : BaseWebLayerTest() {
    
    init {
        Given("POST /api/v1/users 요청") {
            When("유효한 사용자 생성 요청을 보내면") {
                val request = CreateUserRequest(
                    email = "test@example.com",
                    password = "password123!",
                    name = "테스트 사용자"
                )
                
                val expectedUser = fixture<User> {
                    property(User::email) { Email("test@example.com") }
                }
                
                every { userService.createUser(any()) } returns expectedUser
                
                val result = mockMvc.post("/api/v1/users") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }
                
                Then("201 Created 응답을 받는다") {
                    result.andExpect {
                        status { isCreated() }
                        header { exists("Location") }
                        jsonPath("$.email") { value("test@example.com") }
                        jsonPath("$.name") { value("테스트 사용자") }
                    }
                }
            }
            
            When("유효하지 않은 요청을 보내면") {
                val invalidRequest = CreateUserRequest(
                    email = "invalid-email",  // 잘못된 이메일
                    password = "123",         // 너무 짧은 비밀번호
                    name = ""                 // 빈 이름
                )
                
                val result = mockMvc.post("/api/v1/users") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(invalidRequest)
                }
                
                Then("400 Bad Request 응답을 받는다") {
                    result.andExpect {
                        status { isBadRequest() }
                        jsonPath("$.code") { value("VALIDATION_ERROR") }
                        jsonPath("$.errors") { isArray() }
                    }
                }
            }
        }
    }
}
```

## 통합 테스트 패턴

### 1. TestContainers 활용
```kotlin
@SpringBootTest
@Testcontainers
class UserIntegrationTest : BehaviorSpec() {
    
    companion object {
        @JvmStatic
        @Container
        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:15")
            .apply {
                withDatabaseName("testdb")
                withUsername("test")
                withPassword("test")
            }
        
        @JvmStatic
        @Container
        val redisContainer = GenericContainer<Nothing>("redis:7-alpine")
            .apply {
                withExposedPorts(6379)
            }
        
        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
            
            registry.add("spring.data.redis.host", redisContainer::getHost)
            registry.add("spring.data.redis.port") { redisContainer.getMappedPort(6379) }
        }
    }
    
    @Autowired
    private lateinit var userService: UserService
    
    @Autowired
    private lateinit var userRepository: UserRepository
    
    init {
        Given("실제 데이터베이스가 연결된 환경에서") {
            When("사용자를 생성하고 조회하면") {
                val command = CreateUserCommand(
                    email = Email("integration@test.com"),
                    password = "password123!",
                    name = "통합테스트"
                )
                
                val createdUser = userService.createUser(command)
                val foundUser = userRepository.findById(createdUser.id)
                
                Then("데이터가 올바르게 저장되고 조회된다") {
                    foundUser shouldNotBe null
                    foundUser!!.email shouldBe command.email
                    foundUser.name shouldBe command.name
                }
            }
        }
    }
}
```

### 2. E2E 테스트
```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserE2ETest(
    @Autowired private val testRestTemplate: TestRestTemplate
) : BehaviorSpec() {
    
    init {
        Given("사용자 관리 API가 준비된 상태에서") {
            When("사용자 생성 → 조회 → 수정 → 삭제 플로우를 실행하면") {
                // 1. 사용자 생성
                val createRequest = CreateUserRequest(
                    email = "e2e@test.com",
                    password = "password123!",
                    name = "E2E 테스트"
                )
                
                val createResponse = testRestTemplate.postForEntity(
                    "/api/v1/users",
                    createRequest,
                    UserResponse::class.java
                )
                
                val userId = createResponse.body!!.id
                
                // 2. 사용자 조회
                val getResponse = testRestTemplate.getForEntity(
                    "/api/v1/users/$userId",
                    UserResponse::class.java
                )
                
                // 3. 사용자 수정
                val updateRequest = UpdateUserRequest(
                    name = "수정된 이름"
                )
                
                val updateResponse = testRestTemplate.exchange(
                    "/api/v1/users/$userId",
                    HttpMethod.PUT,
                    HttpEntity(updateRequest),
                    UserResponse::class.java
                )
                
                // 4. 사용자 삭제
                testRestTemplate.delete("/api/v1/users/$userId")
                
                val deletedGetResponse = testRestTemplate.getForEntity(
                    "/api/v1/users/$userId",
                    ErrorResponse::class.java
                )
                
                Then("모든 단계가 성공적으로 실행된다") {
                    // 생성 검증
                    createResponse.statusCode shouldBe HttpStatus.CREATED
                    createResponse.body!!.email shouldBe "e2e@test.com"
                    
                    // 조회 검증
                    getResponse.statusCode shouldBe HttpStatus.OK
                    getResponse.body!!.name shouldBe "E2E 테스트"
                    
                    // 수정 검증
                    updateResponse.statusCode shouldBe HttpStatus.OK
                    updateResponse.body!!.name shouldBe "수정된 이름"
                    
                    // 삭제 검증
                    deletedGetResponse.statusCode shouldBe HttpStatus.NOT_FOUND
                }
            }
        }
    }
}
```

## 성능 테스트

### 1. JMH 벤치마크
```kotlin
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
class UserServiceBenchmark {
    
    private lateinit var userService: UserService
    private lateinit var testData: List<CreateUserCommand>
    
    @Setup
    fun setup() {
        // 테스트 데이터 준비
        testData = (1..1000).map { index ->
            CreateUserCommand(
                email = Email("user$index@test.com"),
                password = "password123!",
                name = "User $index"
            )
        }
    }
    
    @Benchmark
    fun benchmarkCreateUser(): User {
        return userService.createUser(testData.random())
    }
    
    @Benchmark
    fun benchmarkBatchCreateUsers(): List<User> {
        return userService.createUsers(testData.take(100))
    }
}
```

### 2. 부하 테스트
```kotlin
@SpringBootTest
class LoadTest : BehaviorSpec() {
    
    @Autowired
    private lateinit var userService: UserService
    
    init {
        Given("동시에 많은 사용자 생성 요청이 들어올 때") {
            When("1000개의 동시 요청을 처리하면") {
                val requests = (1..1000).map { index ->
                    CreateUserCommand(
                        email = Email("load$index@test.com"),
                        password = "password123!",
                        name = "Load Test $index"
                    )
                }
                
                val startTime = System.currentTimeMillis()
                
                val results = runBlocking {
                    requests.map { command ->
                        async(Dispatchers.IO) {
                            userService.createUser(command)
                        }
                    }.awaitAll()
                }
                
                val endTime = System.currentTimeMillis()
                val totalTime = endTime - startTime
                
                Then("모든 요청이 성공적으로 처리된다") {
                    results shouldHaveSize 1000
                    results.all { it.id > 0 } shouldBe true
                    totalTime shouldBeLessThan 10000 // 10초 이내
                }
            }
        }
    }
}
```

## 테스트 유틸리티

### 1. 데이터 빌더
```kotlin
class UserTestDataBuilder {
    private var email: String = "test@example.com"
    private var name: String = "테스트 사용자"
    private var status: UserStatus = UserStatus.ACTIVE
    private var createdAt: LocalDateTime = LocalDateTime.now()
    
    fun withEmail(email: String) = apply { this.email = email }
    fun withName(name: String) = apply { this.name = name }
    fun withStatus(status: UserStatus) = apply { this.status = status }
    fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
    
    fun build(): User = User(
        id = UserId.generate(),
        email = Email(email),
        name = name,
        status = status,
        createdAt = createdAt
    )
    
    fun buildEntity(): UserEntity = UserEntity(
        email = Email(email),
        name = name,
        status = status
    ).apply {
        this.createdAt = this@UserTestDataBuilder.createdAt
    }
}

// DSL 스타일 사용
fun testUser(init: UserTestDataBuilder.() -> Unit = {}): User =
    UserTestDataBuilder().apply(init).build()

// 사용 예시
val user = testUser {
    withEmail("custom@test.com")
    withStatus(UserStatus.INACTIVE)
}
```

### 2. 매처 확장
```kotlin
// 커스텀 매처
fun UserEntity.shouldHaveValidEmail() {
    this.email.value shouldMatch Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
}

fun Collection<User>.shouldAllBeActive() {
    this.all { it.status == UserStatus.ACTIVE } shouldBe true
}

// 날짜 매처
infix fun LocalDateTime.shouldBeCloseTo(other: LocalDateTime) {
    val duration = Duration.between(this, other)
    duration.abs() shouldBeLessThan Duration.ofSeconds(1)
}
```

## 테스트 품질 체크리스트

### 코드 커버리지
- [ ] 라인 커버리지 80% 이상
- [ ] 브랜치 커버리지 70% 이상  
- [ ] 중요 비즈니스 로직 100% 커버리지

### 테스트 품질
- [ ] 각 테스트가 독립적인가?
- [ ] 테스트명이 명확한가?
- [ ] Given-When-Then 패턴을 따르는가?
- [ ] 적절한 수준의 모킹을 사용하는가?

### 성능
- [ ] 단위 테스트가 충분히 빠른가? (< 100ms)
- [ ] 통합 테스트 시간이 합리적인가? (< 30초)
- [ ] 테스트 병렬 실행이 가능한가?