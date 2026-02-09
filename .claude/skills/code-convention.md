---
name: code-convention
description: 코드 컨벤션 가이드. 코드 작성, Entity/Service/Controller/DTO 구현 시 참조
---

# 코드 컨벤션

Metamong 프로젝트의 코드 작성 규칙과 스타일 가이드입니다.

## 네이밍 규칙

| 대상 | 규칙 | 예시 |
|------|------|------|
| Class | UpperCamelCase | `UserCommandService` |
| Function/Property | lowerCamelCase | `getUserById` |
| Constant | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Package | lowercase | `com.metamong.domain` |
| Table/Column | lower_snake_case | `user_id` |

---

## 코드 품질 기준

| 구분 | 권장 | 최대 |
|------|------|------|
| 파일 | 200-400줄 | 800줄 |
| 함수 | 20-30줄 | 50줄 |
| 중첩 | 2-3단계 | 4단계 |

---

## Kotlin 스타일 가이드

### 필수 규칙

```kotlin
// ✅ Trailing comma 필수
fun createUser(
    name: String,
    email: String,
)

// ✅ runCatching 사용 (try-catch 금지)
runCatching { riskyOperation() }
    .onSuccess { handleSuccess(it) }
    .onFailure { handleError(it) }

// ✅ requireNotNull, checkNotNull 사용 (!! 금지)
val user = requireNotNull(userRepository.findByIdOrNull(id)) {
    "User not found: $id"
}

// ✅ 커스텀 예외와 함께
val user = userRepository.findByIdOrNull(id)
    ?: throw UserNotFoundException("User not found: $id")
```

### 불변성 원칙

```kotlin
// ✅ val 우선 사용
val immutableList = listOf(1, 2, 3)
val user = User(name = "John")

// ❌ var는 꼭 필요한 경우만
var counter = 0  // 루프 내부 등 불가피한 경우만
```

### Null Safety

```kotlin
// ✅ 안전한 호출 연산자
user?.name?.uppercase()

// ✅ 엘비스 연산자
val name = user?.name ?: "Unknown"

// ❌ 절대 금지
val name = user!!.name  // NPE 위험
```

### 함수형 프로그래밍

```kotlin
// ✅ 고차함수와 람다 활용
users.filter { it.isActive }
    .map { it.toDto() }
    .sortedBy { it.createdAt }

// ✅ 스코프 함수 적절히 사용
user?.let { 
    updateUser(it)
    notifyUser(it)
}

// ✅ apply로 초기화
val user = User().apply {
    name = "John"
    email = "john@example.com"
}
```

---

## 클래스 구조

### Service 클래스

```kotlin
@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    // 1. companion object (상수, 로거)
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val MAX_LOGIN_ATTEMPTS = 5
    }
    
    // 2. 주요 비즈니스 메서드
    fun createUser(request: CreateUserRequest): UserResponse {
        validateRequest(request)
        
        val user = User(
            name = request.name,
            email = request.email,
            password = passwordEncoder.encode(request.password),
        )
        
        return userRepository.save(user)
            .toResponse()
    }
    
    // 3. 보조 private 메서드
    private fun validateRequest(request: CreateUserRequest) {
        require(request.email.contains("@")) {
            "Invalid email format"
        }
    }
}
```

### Controller 클래스

```kotlin
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {
    @PostMapping
    fun createUser(
        @Valid @RequestBody request: CreateUserRequest,
    ): ResponseEntity<UserResponse> {
        val response = userService.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}
```

### Entity 클래스

```kotlin
@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false, length = 100)
    var name: String,
    
    @Column(nullable = false, unique = true)
    val email: String,
    
    @Column(nullable = false)
    var password: String,
    
    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    fun updateName(newName: String) {
        require(newName.isNotBlank()) { "Name cannot be blank" }
        this.name = newName
    }
}
```

### DTO 클래스

```kotlin
// Request DTO
data class CreateUserRequest(
    @field:NotBlank(message = "이름은 필수입니다")
    val name: String,
    
    @field:Email(message = "유효한 이메일 형식이 아닙니다")
    val email: String,
    
    @field:Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    val password: String,
)

// Response DTO
data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val createdAt: LocalDateTime,
)
```

---

## 테스트 컨벤션

### Kotest BehaviorSpec 스타일

```kotlin
class UserServiceTest : BehaviorSpec({
    val fixture = kotlinFixture()
    val mockRepository = mockk<UserRepository>()
    val service = UserService(mockRepository)
    
    Given("사용자 생성 요청이 있을 때") {
        val request = fixture<CreateUserRequest>()
        
        When("유효한 데이터로 요청하면") {
            every { mockRepository.save(any()) } returns fixture<User>()
            val result = service.createUser(request)
            
            Then("사용자가 생성된다") {
                result shouldNotBe null
                verify { mockRepository.save(any()) }
            }
        }
        
        When("중복된 이메일로 요청하면") {
            every { mockRepository.existsByEmail(request.email) } returns true
            
            Then("예외가 발생한다") {
                shouldThrow<DuplicateEmailException> {
                    service.createUser(request)
                }
            }
        }
    }
})
```

---

## 커밋 메시지 규칙

```
[TYPE] 제목

본문 (선택)

TYPE: feat|fix|docs|style|refactor|test|chore
```

예시:
```
[feat] 사용자 회원가입 기능 구현

- 이메일 중복 검증 추가
- 비밀번호 암호화 적용
- 회원가입 API 엔드포인트 생성
```

---

## ktlint 설정

프로젝트의 ktlint 규칙이 자동으로 적용됩니다:
- git commit 시 자동 포맷팅 (settings.local.json에 설정됨)
- `./gradlew ktlintFormat` 명령으로 수동 실행 가능

주요 규칙:
- 들여쓰기: 4 spaces
- 최대 줄 길이: 120자
- Trailing comma 필수
- Wildcard imports 금지