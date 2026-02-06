# Testing Guidelines

## 테스트 원칙

### 테스트 커버리지
- **Unit Test**: 80% 이상
- **Integration Test**: 주요 시나리오 100%
- **E2E Test**: Critical Path 100%

### TDD (Test-Driven Development)
1. **RED**: 실패하는 테스트 작성
2. **GREEN**: 테스트 통과하는 최소 코드 작성
3. **REFACTOR**: 코드 개선

## Kotest BehaviorSpec 스타일

### 기본 구조
```kotlin
class UserServiceTest : BehaviorSpec({
    // 1. Fixture 선언
    val fixture = kotlinFixture()
    
    // 2. Mock 객체 선언
    val mockUserRepository = mockk<UserRepository>()
    val mockEmailService = mockk<EmailService>()
    
    // 3. 테스트 대상 생성
    val userService = UserService(
        userRepository = mockUserRepository,
        emailService = mockEmailService
    )
    
    // 4. 테스트 시나리오
    Given("사용자 생성 요청이 있을 때") {
        val request = fixture<CreateUserRequest> {
            property(CreateUserRequest::email) { "test@example.com" }
        }
        
        When("유효한 데이터로 요청하면") {
            val user = fixture<User>()
            every { mockUserRepository.save(any()) } returns user
            every { mockEmailService.sendWelcomeEmail(any()) } just Runs
            
            val result = userService.createUser(request)
            
            Then("사용자가 생성된다") {
                result shouldNotBe null
                result.email shouldBe "test@example.com"
            }
            
            Then("환영 이메일이 발송된다") {
                verify(exactly = 1) { 
                    mockEmailService.sendWelcomeEmail("test@example.com") 
                }
            }
        }
        
        When("이미 존재하는 이메일로 요청하면") {
            every { 
                mockUserRepository.existsByEmail(any()) 
            } returns true
            
            Then("예외가 발생한다") {
                shouldThrow<DuplicateEmailException> {
                    userService.createUser(request)
                }
            }
        }
    }
})
```

## MockK 사용법

### Mock 생성
```kotlin
// 일반 Mock
val mockService = mockk<UserService>()

// Relaxed Mock (기본값 반환)
val mockService = mockk<UserService>(relaxed = true)

// Spy (실제 객체 부분 Mock)
val spyService = spyk(UserService())
```

### Stubbing
```kotlin
// 반환값 지정
every { mockRepository.findById(1L) } returns user

// 여러 호출에 다른 값 반환
every { mockRepository.count() } returnsMany listOf(1, 2, 3)

// 예외 발생
every { mockRepository.save(any()) } throws RuntimeException("DB Error")

// void 메서드
every { mockService.sendEmail(any()) } just Runs

// 조건부 반환
every { 
    mockRepository.findByAge(more(18)) 
} returns listOf(adultUser)

// 람다로 동적 반환
every { 
    mockRepository.findById(any()) 
} answers { 
    User(id = arg(0))
}
```

### Verification
```kotlin
// 호출 검증
verify { mockRepository.save(any()) }

// 호출 횟수 검증
verify(exactly = 1) { mockRepository.save(any()) }
verify(atLeast = 2) { mockRepository.findById(any()) }
verify(atMost = 3) { mockRepository.count() }

// 호출 순서 검증
verifyOrder {
    mockRepository.findById(1L)
    mockRepository.save(any())
}

// 호출되지 않음 검증
verify(exactly = 0) { mockRepository.delete(any()) }

// 매개변수 검증
verify { 
    mockRepository.save(
        match { it.email == "test@example.com" }
    ) 
}
```

## 테스트 데이터 생성

### KotlinFixture 활용
```kotlin
private val fixture = kotlinFixture()

// 기본 생성
val user = fixture<User>()

// 특정 필드 지정
val user = fixture<User> {
    property(User::email) { "test@example.com" }
    property(User::age) { 25 }
}

// 리스트 생성
val users = fixture<List<User>> {
    repeatCount { 5 }
}

// 중첩 객체
val order = fixture<Order> {
    property(Order::user) { 
        fixture<User> {
            property(User::id) { 1L }
        }
    }
}
```

### 테스트 데이터 Builder
```kotlin
object TestDataBuilder {
    fun createUser(
        id: Long = 1L,
        email: String = "test@example.com",
        nickname: String = "테스터"
    ) = User(
        id = id,
        email = email,
        nickname = nickname
    )
    
    fun createPost(
        userId: Long = 1L,
        title: String = "테스트 제목"
    ) = Post(
        userId = userId,
        title = title,
        content = "테스트 내용"
    )
}
```

## Kotest Matchers

### 기본 매처
```kotlin
// 동등성
result shouldBe expected
result shouldNotBe unexpected

// null 체크
result shouldNotBeNull
nullable shouldBeNull

// 타입 체크
result shouldBeInstanceOf<User>()

// 예외 검증
shouldThrow<IllegalArgumentException> {
    validateAge(-1)
}

val exception = shouldThrow<CustomException> {
    riskyOperation()
}
exception.message shouldContain "error"
```

### 컬렉션 매처
```kotlin
// 크기
list shouldHaveSize 3
list.shouldBeEmpty()
list.shouldNotBeEmpty()

// 포함
list shouldContain element
list shouldContainAll listOf(1, 2, 3)
list shouldNotContain 4

// 순서
list shouldBeSorted
list shouldContainInOrder listOf(1, 2, 3)

// 조건
list.shouldAll { it > 0 }
list.shouldAny { it == 5 }
list.shouldNone { it < 0 }
```

### 문자열 매처
```kotlin
string shouldStartWith "Hello"
string shouldEndWith "World"
string shouldContain "test"
string shouldMatch Regex("\\d+")
string.shouldBeBlank()
```

### 숫자 매처
```kotlin
number shouldBeGreaterThan 10
number shouldBeLessThanOrEqual 100
number shouldBeBetween Pair(1, 10)
double shouldBe (3.14 plusOrMinus 0.01)
```

## 통합 테스트

### @SpringBootTest
```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class UserIntegrationTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val userRepository: UserRepository
) : BehaviorSpec({
    
    beforeEach {
        userRepository.deleteAll()
    }
    
    Given("사용자 생성 API") {
        When("POST /api/v1/users 요청시") {
            val request = """
                {
                    "email": "test@example.com",
                    "nickname": "테스터",
                    "password": "password123"
                }
            """.trimIndent()
            
            val result = mockMvc.post("/api/v1/users") {
                contentType = MediaType.APPLICATION_JSON
                content = request
            }
            
            Then("201 Created 응답") {
                result.andExpect {
                    status { isCreated() }
                    jsonPath("$.email") { value("test@example.com") }
                }
            }
            
            Then("DB에 저장됨") {
                val user = userRepository.findByEmail("test@example.com")
                user shouldNotBe null
            }
        }
    }
})
```

### TestContainers
```kotlin
@TestConfiguration
class TestContainersConfig {
    
    companion object {
        @Container
        val mysqlContainer = MySQLContainer<Nothing>("mysql:8.0").apply {
            withDatabaseName("test")
            withUsername("test")
            withPassword("test")
        }
        
        @Container
        val redisContainer = GenericContainer<Nothing>("redis:7").apply {
            withExposedPorts(6379)
        }
        
        init {
            mysqlContainer.start()
            redisContainer.start()
        }
    }
    
    @Bean
    fun dataSource(): DataSource {
        return DataSourceBuilder.create()
            .url(mysqlContainer.jdbcUrl)
            .username(mysqlContainer.username)
            .password(mysqlContainer.password)
            .build()
    }
}
```

## 테스트 격리

### 트랜잭션 롤백
```kotlin
@SpringBootTest
@Transactional
@Rollback
class RepositoryTest : BehaviorSpec({
    // 각 테스트 후 자동 롤백
})
```

### 테스트 데이터 정리
```kotlin
beforeEach {
    // 테스트 전 데이터 정리
    userRepository.deleteAll()
}

afterEach {
    // 테스트 후 정리
    clearAllMocks()
}
```

## 테스트 성능 최적화

### 테스트 슬라이스
```kotlin
// 웹 레이어만 테스트
@WebMvcTest(UserController::class)

// JPA 레이어만 테스트
@DataJpaTest

// JSON 직렬화만 테스트
@JsonTest
```

### 병렬 실행
```kotlin
// build.gradle.kts
tasks.test {
    maxParallelForks = Runtime.getRuntime().availableProcessors()
}
```

## 테스트 명명 규칙

### 한글 사용
```kotlin
Given("사용자가 로그인을 시도할 때")
When("올바른 비밀번호를 입력하면")
Then("로그인에 성공한다")
```

### 메서드명 (JUnit 스타일 사용시)
```kotlin
@Test
fun `createUser - 유효한 이메일 - 사용자 생성 성공`() {
    // 테스트
}
```

## 테스트 문서화

### 시나리오 명세
```kotlin
/**
 * 사용자 생성 시나리오
 * 1. 이메일 중복 검사
 * 2. 비밀번호 암호화
 * 3. DB 저장
 * 4. 환영 이메일 발송
 */
Given("사용자 생성 시나리오") {
    // 테스트
}
```

## CI/CD 통합

### GitHub Actions
```yaml
- name: Run tests
  run: ./gradlew test

- name: Generate coverage report
  run: ./gradlew jacocoTestReport

- name: Upload coverage
  uses: codecov/codecov-action@v3
```

## 테스트 체크리스트

- [ ] Given-When-Then 구조 준수
- [ ] 한 테스트에 하나의 검증
- [ ] 테스트 격리 보장
- [ ] Mock 최소화
- [ ] 의미있는 테스트 이름
- [ ] 엣지 케이스 포함
- [ ] 실패 케이스 테스트
- [ ] 테스트 실행 시간 < 1초