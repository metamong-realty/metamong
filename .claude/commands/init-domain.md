---
description: 새 도메인의 전체 구조를 자동 생성합니다 (Entity, Repository, Service, Controller, DTO, Test)
---

# Init Domain Command

새로운 도메인의 전체 레이어 구조를 자동으로 생성하는 명령어입니다.

## Usage

```bash
/init-domain <domain-name> [options]
```

## Examples

```bash
# 기본 도메인 생성
/init-domain user

# 특정 옵션과 함께 생성
/init-domain product --with-audit --with-soft-delete

# 복합 도메인 생성
/init-domain order-item --parent order
```

## What This Command Does

### 1. Domain Layer 생성
```kotlin
// domain/user/model/UserEntity.kt
@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(unique = true, nullable = false)
    val email: String,
    
    var nickname: String,
    
    @Enumerated(EnumType.STRING)
    val status: UserStatus = UserStatus.ACTIVE
) : BaseEntity()

// domain/user/model/UserStatus.kt
enum class UserStatus {
    ACTIVE, INACTIVE, SUSPENDED
}
```

### 2. Repository Layer 생성
```kotlin
// domain/user/repository/UserRepository.kt
interface UserRepository {
    fun save(user: UserEntity): UserEntity
    fun findById(id: Long): UserEntity?
    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean
    fun deleteById(id: Long)
}

// infra/persistence/user/UserRepositoryImpl.kt
@Repository
class UserRepositoryImpl(
    private val jpaRepository: UserJpaRepository,
    private val userQueryRepository: UserQueryRepository
) : UserRepository {
    // 구현
}

// infra/persistence/user/UserJpaRepository.kt
interface UserJpaRepository : JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean
}

// infra/persistence/user/UserQueryRepository.kt
interface UserQueryRepository {
    fun findUsersWithPaging(pageable: Pageable): Page<UserProjection>
}
```

### 3. Application Layer 생성
```kotlin
// application/user/service/UserCommandService.kt
@Service
@Transactional
class UserCommandService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun createUser(command: CreateUserCommand): UserDto {
        validateEmailDuplicate(command.email)
        
        val user = UserEntity(
            email = command.email,
            nickname = command.nickname,
            password = passwordEncoder.encode(command.password)
        )
        
        return UserDto.from(userRepository.save(user))
    }
}

// application/user/service/UserQueryService.kt
@Service
@Transactional(readOnly = true)
class UserQueryService(
    private val userRepository: UserRepository
) {
    fun getUser(id: Long): UserDto? {
        return userRepository.findById(id)?.let { UserDto.from(it) }
    }
    
    fun searchUsers(pageable: Pageable): Page<UserDto> {
        return userRepository.findUsersWithPaging(pageable)
            .map { UserDto.from(it) }
    }
}
```

### 4. Presentation Layer 생성
```kotlin
// presentation/api/user/UserController.kt
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "사용자 관리 API")
class UserController(
    private val userCommandService: UserCommandService,
    private val userQueryService: UserQueryService
) {
    
    @PostMapping
    @Operation(summary = "사용자 생성")
    fun createUser(
        @Valid @RequestBody request: CreateUserRequest
    ): ResponseEntity<UserResponse> {
        val user = userCommandService.createUser(request.toCommand())
        return ResponseEntity
            .created(URI.create("/api/v1/users/${user.id}"))
            .body(UserResponse.from(user))
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "사용자 조회")
    fun getUser(@PathVariable id: Long): ResponseEntity<UserResponse> {
        val user = userQueryService.getUser(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(UserResponse.from(user))
    }
}
```

### 5. DTO 생성
```kotlin
// presentation/api/user/dto/CreateUserRequest.kt
@Schema(description = "사용자 생성 요청")
data class CreateUserRequest(
    @Schema(description = "이메일", example = "user@example.com")
    @field:NotBlank @field:Email val email: String,
    
    @Schema(description = "닉네임", example = "홍길동")
    @field:NotBlank @field:Size(min = 2, max = 20) val nickname: String,
    
    @Schema(description = "비밀번호")
    @field:NotBlank @field:Size(min = 8) val password: String
) {
    fun toCommand() = CreateUserCommand(
        email = email,
        nickname = nickname,
        password = password
    )
}

// presentation/api/user/dto/UserResponse.kt
@Schema(description = "사용자 응답")
data class UserResponse(
    val id: Long,
    val email: String,
    val nickname: String,
    val status: UserStatus,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(user: UserDto) = UserResponse(
            id = user.id,
            email = user.email,
            nickname = user.nickname,
            status = user.status,
            createdAt = user.createdAt
        )
    }
}

// application/user/dto/UserDto.kt
data class UserDto(
    val id: Long,
    val email: String,
    val nickname: String,
    val status: UserStatus,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(entity: UserEntity) = UserDto(
            id = entity.id,
            email = entity.email,
            nickname = entity.nickname,
            status = entity.status,
            createdAt = entity.createdAt
        )
    }
}
```

### 6. Test 생성
```kotlin
// test/.../application/user/service/UserCommandServiceTest.kt
class UserCommandServiceTest : BehaviorSpec({
    val fixture = kotlinFixture()
    val mockUserRepository = mockk<UserRepository>()
    val mockPasswordEncoder = mockk<PasswordEncoder>()
    
    val userCommandService = UserCommandService(
        userRepository = mockUserRepository,
        passwordEncoder = mockPasswordEncoder
    )
    
    Given("사용자 생성 명령이 주어졌을 때") {
        val command = fixture<CreateUserCommand>()
        val encodedPassword = "encoded_password"
        val savedUser = fixture<UserEntity>()
        
        every { mockUserRepository.existsByEmail(command.email) } returns false
        every { mockPasswordEncoder.encode(command.password) } returns encodedPassword
        every { mockUserRepository.save(any()) } returns savedUser
        
        When("사용자를 생성하면") {
            val result = userCommandService.createUser(command)
            
            Then("사용자가 생성된다") {
                result shouldNotBe null
                verify { mockUserRepository.save(any()) }
            }
        }
    }
})

// test/.../presentation/api/user/UserControllerTest.kt
@WebMvcTest(UserController::class)
class UserControllerTest(
    @Autowired val mockMvc: MockMvc,
    @MockkBean val userCommandService: UserCommandService,
    @MockkBean val userQueryService: UserQueryService
) : BehaviorSpec({
    
    Given("POST /api/v1/users") {
        When("유효한 사용자 생성 요청시") {
            val request = """
                {
                    "email": "test@example.com",
                    "nickname": "테스터",
                    "password": "password123"
                }
            """.trimIndent()
            
            val userDto = UserDto(1L, "test@example.com", "테스터", UserStatus.ACTIVE, LocalDateTime.now())
            every { userCommandService.createUser(any()) } returns userDto
            
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
        }
    }
})
```

## Generated File Structure

```
src/main/kotlin/com/metamong/
├── domain/
│   └── user/
│       ├── model/
│       │   ├── UserEntity.kt
│       │   └── UserStatus.kt
│       └── repository/
│           └── UserRepository.kt
├── application/
│   └── user/
│       ├── service/
│       │   ├── UserCommandService.kt
│       │   └── UserQueryService.kt
│       ├── dto/
│       │   ├── UserDto.kt
│       │   └── CreateUserCommand.kt
│       └── command/
├── infra/
│   └── persistence/
│       └── user/
│           ├── UserRepositoryImpl.kt
│           ├── UserJpaRepository.kt
│           ├── UserQueryRepository.kt
│           └── UserQueryRepositoryImpl.kt
└── presentation/
    └── api/
        └── user/
            ├── UserController.kt
            └── dto/
                ├── CreateUserRequest.kt
                ├── UpdateUserRequest.kt
                └── UserResponse.kt

src/test/kotlin/com/metamong/
├── application/user/service/
│   ├── UserCommandServiceTest.kt
│   └── UserQueryServiceTest.kt
├── infra/persistence/user/
│   └── UserRepositoryTest.kt
└── presentation/api/user/
    └── UserControllerTest.kt
```

## Options

### --with-audit
BaseEntity 대신 BaseAuditEntity 상속
```kotlin
class UserEntity : BaseAuditEntity()  // createdBy, updatedBy 포함
```

### --with-soft-delete
소프트 삭제 기능 추가
```kotlin
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
class UserEntity {
    var deletedAt: LocalDateTime? = null
}
```

### --parent <parent-domain>
부모 도메인과의 연관관계 설정
```kotlin
// order-item --parent order
class OrderItemEntity {
    val orderId: Long  // 부모 도메인 참조
}
```

## Post-Generation Tasks

명령어 실행 후 수동으로 확인/수정할 사항들:

### 1. 비즈니스 로직 구현
- [ ] Entity의 비즈니스 메서드 추가
- [ ] 도메인 검증 규칙 구현
- [ ] 복잡한 쿼리 작성

### 2. 관계 설정
- [ ] 다른 도메인과의 연관관계 정의
- [ ] 외래키 제약조건 설정

### 3. 보안 설정
- [ ] Controller 인증/인가 설정
- [ ] 민감정보 암호화

### 4. 테스트 보완
- [ ] 엣지 케이스 테스트 추가
- [ ] 통합 테스트 작성
- [ ] 성능 테스트 고려

## Integration with Other Commands

이 명령어와 함께 사용하면 좋은 다른 명령어들:

```bash
# 도메인 생성 후 TDD로 구현
/init-domain user
/tdd "사용자 회원가입 기능"

# API 테스트 생성
/api-create POST /api/v1/users

# 보안 검사
/security-audit user
```