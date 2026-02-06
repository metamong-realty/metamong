---
description: Test-Driven Development 워크플로를 구현합니다 (RED-GREEN-REFACTOR 사이클)
---

# TDD Command

Test-Driven Development 방식으로 기능을 구현하는 명령어입니다.

## Usage

```bash
/tdd "<feature-description>" [options]
```

## Examples

```bash
# 기본 TDD 워크플로
/tdd "사용자 회원가입 기능"

# 특정 레이어만 테스트
/tdd "이메일 중복 검증" --layer service

# 성능 요구사항 포함
/tdd "게시글 검색 기능" --performance "응답시간 < 200ms"
```

## TDD Workflow

### Phase 1: RED (실패하는 테스트 작성)

#### 1.1 요구사항 분석
```kotlin
/**
 * 사용자 회원가입 기능 요구사항:
 * 1. 이메일과 닉네임은 필수 입력
 * 2. 이메일 중복 검사 수행
 * 3. 비밀번호는 8자 이상, 영문+숫자+특수문자 포함
 * 4. 성공시 JWT 토큰 반환
 * 5. 회원가입 완료 이메일 발송
 */
```

#### 1.2 테스트 시나리오 작성
```kotlin
class UserRegistrationTest : BehaviorSpec({
    val fixture = kotlinFixture()
    val mockUserRepository = mockk<UserRepository>()
    val mockEmailService = mockk<EmailService>()
    val mockPasswordEncoder = mockk<PasswordEncoder>()
    val mockJwtProvider = mockk<JwtProvider>()
    
    val userService = UserService(
        userRepository = mockUserRepository,
        emailService = mockEmailService,
        passwordEncoder = mockPasswordEncoder,
        jwtProvider = mockJwtProvider
    )
    
    Given("유효한 회원가입 요청이 있을 때") {
        val request = CreateUserRequest(
            email = "test@example.com",
            nickname = "테스터",
            password = "password123!"
        )
        
        every { mockUserRepository.existsByEmail(any()) } returns false
        every { mockPasswordEncoder.encode(any()) } returns "encoded_password"
        every { mockUserRepository.save(any()) } returns fixture<User>()
        every { mockJwtProvider.createAccessToken(any()) } returns "jwt_token"
        every { mockEmailService.sendWelcomeEmail(any()) } just Runs
        
        When("회원가입을 수행하면") {
            val result = userService.register(request)
            
            Then("사용자가 생성된다") {
                result shouldNotBe null
                result.token shouldNotBe null
            }
            
            Then("환영 이메일이 발송된다") {
                verify(exactly = 1) { 
                    mockEmailService.sendWelcomeEmail("test@example.com") 
                }
            }
        }
    }
    
    Given("중복된 이메일로 회원가입 요청할 때") {
        val request = CreateUserRequest(
            email = "duplicate@example.com",
            nickname = "테스터",
            password = "password123!"
        )
        
        every { mockUserRepository.existsByEmail("duplicate@example.com") } returns true
        
        When("회원가입을 수행하면") {
            Then("중복 이메일 예외가 발생한다") {
                shouldThrow<DuplicateEmailException> {
                    userService.register(request)
                }
            }
        }
    }
    
    Given("약한 비밀번호로 회원가입 요청할 때") {
        val request = CreateUserRequest(
            email = "test@example.com",
            nickname = "테스터",
            password = "123"  // 약한 비밀번호
        )
        
        When("회원가입을 수행하면") {
            Then("비밀번호 검증 예외가 발생한다") {
                shouldThrow<WeakPasswordException> {
                    userService.register(request)
                }
            }
        }
    }
})
```

#### 1.3 API 테스트 작성
```kotlin
@WebMvcTest(UserController::class)
class UserRegistrationApiTest(
    @Autowired val mockMvc: MockMvc,
    @MockkBean val userService: UserService
) : BehaviorSpec({
    
    Given("POST /api/v1/auth/register") {
        When("유효한 회원가입 데이터로 요청하면") {
            val request = """
                {
                    "email": "test@example.com",
                    "nickname": "테스터",
                    "password": "password123!"
                }
            """.trimIndent()
            
            val response = AuthResponse(
                token = "jwt_token",
                user = UserDto(1L, "test@example.com", "테스터")
            )
            every { userService.register(any()) } returns response
            
            val result = mockMvc.post("/api/v1/auth/register") {
                contentType = MediaType.APPLICATION_JSON
                content = request
            }
            
            Then("201 Created와 토큰을 반환한다") {
                result.andExpect {
                    status { isCreated() }
                    jsonPath("$.token") { value("jwt_token") }
                    jsonPath("$.user.email") { value("test@example.com") }
                }
            }
        }
        
        When("유효하지 않은 데이터로 요청하면") {
            val invalidRequest = """
                {
                    "email": "invalid-email",
                    "nickname": "",
                    "password": "123"
                }
            """.trimIndent()
            
            val result = mockMvc.post("/api/v1/auth/register") {
                contentType = MediaType.APPLICATION_JSON
                content = invalidRequest
            }
            
            Then("400 Bad Request를 반환한다") {
                result.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.code") { value("VALIDATION_ERROR") }
                }
            }
        }
    }
})
```

### Phase 2: GREEN (테스트를 통과시키는 최소 코드)

#### 2.1 도메인 모델 구현
```kotlin
@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(unique = true, nullable = false)
    val email: String,
    
    var nickname: String,
    
    private var password: String,
    
    @Enumerated(EnumType.STRING)
    val status: UserStatus = UserStatus.ACTIVE,
    
    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun changePassword(newPassword: String, passwordEncoder: PasswordEncoder) {
        validatePasswordStrength(newPassword)
        this.password = passwordEncoder.encode(newPassword)
    }
    
    private fun validatePasswordStrength(password: String) {
        require(password.length >= 8) { "비밀번호는 8자 이상이어야 합니다" }
        require(password.any { it.isUpperCase() }) { "대문자를 포함해야 합니다" }
        require(password.any { it.isLowerCase() }) { "소문자를 포함해야 합니다" }
        require(password.any { it.isDigit() }) { "숫자를 포함해야 합니다" }
        require(password.any { "!@#$%^&*()_+-=[]{}|;':\",./<>?".contains(it) }) { 
            "특수문자를 포함해야 합니다" 
        }
    }
}
```

#### 2.2 Service 구현
```kotlin
@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val emailService: EmailService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    
    fun register(request: CreateUserRequest): AuthResponse {
        validateEmailDuplicate(request.email)
        validatePasswordStrength(request.password)
        
        val user = User(
            email = request.email,
            nickname = request.nickname,
            password = passwordEncoder.encode(request.password)
        )
        
        val savedUser = userRepository.save(user)
        
        // 비동기 이메일 발송
        sendWelcomeEmailAsync(savedUser.email)
        
        val token = jwtProvider.createAccessToken(savedUser.id)
        
        logger.info { "User registered: ${savedUser.email}" }
        
        return AuthResponse(
            token = token,
            user = UserDto.from(savedUser)
        )
    }
    
    private fun validateEmailDuplicate(email: String) {
        if (userRepository.existsByEmail(email)) {
            throw DuplicateEmailException("이미 존재하는 이메일입니다: $email")
        }
    }
    
    private fun validatePasswordStrength(password: String) {
        try {
            // User 도메인 객체의 검증 로직 사용
            val tempUser = User(
                email = "temp@temp.com",
                nickname = "temp",
                password = "temp"
            )
            tempUser.changePassword(password, passwordEncoder)
        } catch (e: IllegalArgumentException) {
            throw WeakPasswordException(e.message ?: "약한 비밀번호입니다")
        }
    }
    
    @Async
    private fun sendWelcomeEmailAsync(email: String) {
        try {
            emailService.sendWelcomeEmail(email)
        } catch (e: Exception) {
            logger.error(e) { "Failed to send welcome email to $email" }
        }
    }
}
```

#### 2.3 Controller 구현
```kotlin
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "인증 관련 API")
class AuthController(
    private val userService: UserService
) {
    
    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "회원가입 성공"),
        ApiResponse(responseCode = "400", description = "유효하지 않은 입력"),
        ApiResponse(responseCode = "409", description = "이메일 중복")
    )
    fun register(
        @Valid @RequestBody request: CreateUserRequest
    ): ResponseEntity<AuthResponse> {
        val response = userService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}
```

### Phase 3: REFACTOR (코드 개선)

#### 3.1 비즈니스 로직 분리
```kotlin
@Component
class PasswordValidator {
    
    fun validate(password: String): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (password.length < 8) {
            errors.add("비밀번호는 8자 이상이어야 합니다")
        }
        
        if (!password.any { it.isUpperCase() }) {
            errors.add("대문자를 포함해야 합니다")
        }
        
        if (!password.any { it.isLowerCase() }) {
            errors.add("소문자를 포함해야 합니다")
        }
        
        if (!password.any { it.isDigit() }) {
            errors.add("숫자를 포함해야 합니다")
        }
        
        if (!password.any { "!@#$%^&*()_+-=[]{}|;':\",./<>?".contains(it) }) {
            errors.add("특수문자를 포함해야 합니다")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String>
    )
}
```

#### 3.2 이벤트 기반 아키텍처
```kotlin
// 이벤트 정의
data class UserRegisteredEvent(
    val userId: Long,
    val email: String,
    val registeredAt: LocalDateTime = LocalDateTime.now()
)

// Service 수정
@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordValidator: PasswordValidator,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
    private val eventPublisher: ApplicationEventPublisher
) {
    
    fun register(request: CreateUserRequest): AuthResponse {
        validateEmailDuplicate(request.email)
        validatePassword(request.password)
        
        val user = User(
            email = request.email,
            nickname = request.nickname,
            password = passwordEncoder.encode(request.password)
        )
        
        val savedUser = userRepository.save(user)
        
        // 이벤트 발행
        eventPublisher.publishEvent(
            UserRegisteredEvent(
                userId = savedUser.id,
                email = savedUser.email
            )
        )
        
        val token = jwtProvider.createAccessToken(savedUser.id)
        
        return AuthResponse(
            token = token,
            user = UserDto.from(savedUser)
        )
    }
    
    private fun validatePassword(password: String) {
        val result = passwordValidator.validate(password)
        if (!result.isValid) {
            throw WeakPasswordException(result.errors.joinToString(", "))
        }
    }
}

// 이벤트 핸들러
@Component
class UserEventHandler(
    private val emailService: EmailService
) {
    
    @EventListener
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleUserRegistered(event: UserRegisteredEvent) {
        try {
            emailService.sendWelcomeEmail(event.email)
            logger.info { "Welcome email sent to ${event.email}" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to send welcome email to ${event.email}" }
        }
    }
}
```

### Phase 4: 통합 테스트

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserRegistrationIntegrationTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val userRepository: UserRepository,
    @Autowired val jdbcTemplate: JdbcTemplate
) : BehaviorSpec({
    
    beforeEach {
        userRepository.deleteAll()
    }
    
    Given("실제 데이터베이스를 사용한 회원가입 테스트") {
        When("유효한 회원가입 요청을 보내면") {
            val request = """
                {
                    "email": "integration@example.com",
                    "nickname": "통합테스터",
                    "password": "password123!"
                }
            """.trimIndent()
            
            val result = mockMvc.post("/api/v1/auth/register") {
                contentType = MediaType.APPLICATION_JSON
                content = request
            }
            
            Then("201 Created 응답과 함께") {
                result.andExpect {
                    status { isCreated() }
                    jsonPath("$.token") { exists() }
                }
            }
            
            Then("데이터베이스에 사용자가 저장된다") {
                val users = userRepository.findByEmail("integration@example.com")
                users shouldNotBe null
                users?.email shouldBe "integration@example.com"
            }
            
            Then("비밀번호가 암호화되어 저장된다") {
                val encryptedPassword = jdbcTemplate.queryForObject(
                    "SELECT password FROM users WHERE email = ?",
                    String::class.java,
                    "integration@example.com"
                )
                encryptedPassword shouldNotBe "password123!"
                encryptedPassword?.length shouldBeGreaterThan 50
            }
        }
    }
})
```

## TDD Best Practices

### 1. 테스트 명명 규칙
```kotlin
// Good
Given("유효한 이메일로 회원가입할 때")
When("회원가입을 수행하면")
Then("사용자가 생성되고 토큰이 반환된다")

// Bad
@Test
fun testUserRegister() { }
```

### 2. Given-When-Then 구조
```kotlin
Given("테스트 조건 설정") {
    // 테스트 데이터 준비
    val request = CreateUserRequest(...)
    every { mockRepository.save(any()) } returns user
    
    When("실제 동작 수행") {
        val result = service.register(request)
        
        Then("예상 결과 검증") {
            result shouldNotBe null
            verify { mockRepository.save(any()) }
        }
    }
}
```

### 3. 테스트 격리
```kotlin
beforeEach {
    clearAllMocks()  // Mock 초기화
    userRepository.deleteAll()  // 데이터 정리
}
```

### 4. 의미있는 테스트 데이터
```kotlin
val validUser = CreateUserRequest(
    email = "valid@example.com",
    nickname = "유효한사용자",
    password = "StrongPass123!"
)

val invalidUser = CreateUserRequest(
    email = "invalid-email",
    nickname = "",
    password = "123"
)
```

## Performance Requirements

성능 요구사항이 있는 경우:

```kotlin
@Test
fun `회원가입 응답시간 200ms 이하`() {
    val startTime = System.currentTimeMillis()
    
    userService.register(validRequest)
    
    val endTime = System.currentTimeMillis()
    val duration = endTime - startTime
    
    duration shouldBeLessThan 200
}
```

## Code Coverage

TDD 완료 후 커버리지 확인:

```bash
./gradlew jacocoTestReport
```

목표:
- Line Coverage: 80% 이상
- Branch Coverage: 70% 이상
- Method Coverage: 90% 이상