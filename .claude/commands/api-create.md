---
description: RESTful API 엔드포인트를 생성합니다 (Controller, DTO, Service 연동, Swagger 문서화)
---

# API Create Command

새로운 RESTful API 엔드포인트를 생성하는 명령어입니다.

## Usage

```bash
/api-create <method> <path> [options]
```

## Examples

```bash
# 기본 CRUD API 생성
/api-create POST /api/v1/users
/api-create GET /api/v1/users/{id}
/api-create PUT /api/v1/users/{id}
/api-create DELETE /api/v1/users/{id}

# 복합 API 생성
/api-create GET /api/v1/users/{id}/posts --with-pagination
/api-create POST /api/v1/auth/login --no-auth

# 검색 API 생성
/api-create GET /api/v1/products/search --with-filter
```

## What This Command Does

### 1. Controller Method 생성

#### POST API (생성)
```kotlin
@PostMapping
@Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다")
@ApiResponses(
    ApiResponse(responseCode = "201", description = "사용자 생성 성공"),
    ApiResponse(responseCode = "400", description = "유효하지 않은 요청"),
    ApiResponse(responseCode = "409", description = "이메일 중복")
)
fun createUser(
    @Valid @RequestBody request: CreateUserRequest
): ResponseEntity<UserResponse> {
    val user = userCommandService.createUser(request.toCommand())
    return ResponseEntity
        .created(URI.create("/api/v1/users/${user.id}"))
        .body(UserResponse.from(user))
}
```

#### GET API (조회)
```kotlin
@GetMapping("/{id}")
@Operation(summary = "사용자 상세 조회", description = "ID로 사용자를 조회합니다")
@ApiResponses(
    ApiResponse(responseCode = "200", description = "조회 성공"),
    ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
)
fun getUser(
    @PathVariable @Schema(description = "사용자 ID", example = "1") id: Long
): ResponseEntity<UserResponse> {
    val user = userQueryService.getUser(id)
        ?: return ResponseEntity.notFound().build()
    return ResponseEntity.ok(UserResponse.from(user))
}
```

#### GET API (목록 조회 with 페이징)
```kotlin
@GetMapping
@Operation(summary = "사용자 목록 조회", description = "페이징된 사용자 목록을 조회합니다")
fun getUsers(
    @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC)
    pageable: Pageable,
    
    @RequestParam(required = false) 
    @Schema(description = "검색 키워드") 
    keyword: String?,
    
    @RequestParam(required = false) 
    @Schema(description = "사용자 상태") 
    status: UserStatus?
): ResponseEntity<Page<UserResponse>> {
    val users = userQueryService.searchUsers(
        keyword = keyword,
        status = status,
        pageable = pageable
    )
    return ResponseEntity.ok(users.map { UserResponse.from(it) })
}
```

#### PUT API (전체 수정)
```kotlin
@PutMapping("/{id}")
@Operation(summary = "사용자 정보 수정", description = "사용자 정보를 수정합니다")
@ApiResponses(
    ApiResponse(responseCode = "200", description = "수정 성공"),
    ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
)
fun updateUser(
    @PathVariable id: Long,
    @Valid @RequestBody request: UpdateUserRequest
): ResponseEntity<UserResponse> {
    val user = userCommandService.updateUser(id, request.toCommand())
        ?: return ResponseEntity.notFound().build()
    return ResponseEntity.ok(UserResponse.from(user))
}
```

#### DELETE API (삭제)
```kotlin
@DeleteMapping("/{id}")
@Operation(summary = "사용자 삭제", description = "사용자를 삭제합니다")
@ApiResponses(
    ApiResponse(responseCode = "204", description = "삭제 성공"),
    ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
)
@ResponseStatus(HttpStatus.NO_CONTENT)
fun deleteUser(
    @PathVariable id: Long
) {
    userCommandService.deleteUser(id)
}
```

### 2. Request/Response DTO 생성

#### Create Request
```kotlin
@Schema(description = "사용자 생성 요청")
data class CreateUserRequest(
    @Schema(description = "이메일", example = "user@example.com", required = true)
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String,
    
    @Schema(description = "닉네임", example = "홍길동", required = true)
    @field:NotBlank(message = "닉네임은 필수입니다")
    @field:Size(min = 2, max = 20, message = "닉네임은 2-20자여야 합니다")
    val nickname: String,
    
    @Schema(description = "비밀번호", required = true)
    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    val password: String
) {
    fun toCommand() = CreateUserCommand(
        email = email,
        nickname = nickname,
        password = password
    )
}
```

#### Update Request
```kotlin
@Schema(description = "사용자 수정 요청")
data class UpdateUserRequest(
    @Schema(description = "닉네임", example = "홍길동수정")
    @field:Size(min = 2, max = 20, message = "닉네임은 2-20자여야 합니다")
    val nickname: String?,
    
    @Schema(description = "프로필 이미지 URL")
    @field:URL(message = "올바른 URL 형식이 아닙니다")
    val profileImageUrl: String?
) {
    fun toCommand(id: Long) = UpdateUserCommand(
        id = id,
        nickname = nickname,
        profileImageUrl = profileImageUrl
    )
}
```

#### Response DTO
```kotlin
@Schema(description = "사용자 응답")
data class UserResponse(
    @Schema(description = "사용자 ID", example = "1")
    val id: Long,
    
    @Schema(description = "이메일", example = "user@example.com")
    val email: String,
    
    @Schema(description = "닉네임", example = "홍길동")
    val nickname: String,
    
    @Schema(description = "상태", example = "ACTIVE")
    val status: UserStatus,
    
    @Schema(description = "생성일시", example = "2024-01-01T00:00:00")
    val createdAt: LocalDateTime,
    
    @Schema(description = "수정일시", example = "2024-01-01T00:00:00")
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(user: UserDto) = UserResponse(
            id = user.id,
            email = user.email,
            nickname = user.nickname,
            status = user.status,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }
}
```

### 3. Service Layer 연동

#### Command 객체 생성
```kotlin
// application/user/command/CreateUserCommand.kt
data class CreateUserCommand(
    val email: String,
    val nickname: String,
    val password: String
)

// application/user/command/UpdateUserCommand.kt
data class UpdateUserCommand(
    val id: Long,
    val nickname: String?,
    val profileImageUrl: String?
)
```

#### Service Method 생성 (없는 경우만)
```kotlin
// UserCommandService에 메서드 추가
fun createUser(command: CreateUserCommand): UserDto {
    validateEmailDuplicate(command.email)
    
    val user = UserEntity(
        email = command.email,
        nickname = command.nickname,
        password = passwordEncoder.encode(command.password)
    )
    
    return UserDto.from(userRepository.save(user))
}

fun updateUser(id: Long, command: UpdateUserCommand): UserDto? {
    val user = userRepository.findById(id) ?: return null
    
    command.nickname?.let { user.nickname = it }
    command.profileImageUrl?.let { user.profileImageUrl = it }
    
    return UserDto.from(userRepository.save(user))
}
```

### 4. 테스트 생성

#### Controller 테스트
```kotlin
@WebMvcTest(UserController::class)
class UserApiTest(
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
                    "password": "password123!"
                }
            """.trimIndent()
            
            val userDto = UserDto(1L, "test@example.com", "테스터", UserStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now())
            every { userCommandService.createUser(any()) } returns userDto
            
            val result = mockMvc.post("/api/v1/users") {
                contentType = MediaType.APPLICATION_JSON
                content = request
            }
            
            Then("201 Created 응답") {
                result.andExpect {
                    status { isCreated() }
                    header { string("Location", "/api/v1/users/1") }
                    jsonPath("$.email") { value("test@example.com") }
                }
            }
        }
        
        When("유효하지 않은 요청시") {
            val invalidRequest = """
                {
                    "email": "invalid-email",
                    "nickname": "",
                    "password": "123"
                }
            """.trimIndent()
            
            val result = mockMvc.post("/api/v1/users") {
                contentType = MediaType.APPLICATION_JSON
                content = invalidRequest
            }
            
            Then("400 Bad Request 응답") {
                result.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.code") { value("VALIDATION_ERROR") }
                }
            }
        }
    }
    
    Given("GET /api/v1/users/{id}") {
        When("존재하는 ID로 요청시") {
            val userDto = UserDto(1L, "test@example.com", "테스터", UserStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now())
            every { userQueryService.getUser(1L) } returns userDto
            
            val result = mockMvc.get("/api/v1/users/1")
            
            Then("200 OK와 사용자 정보 반환") {
                result.andExpect {
                    status { isOk() }
                    jsonPath("$.id") { value(1) }
                    jsonPath("$.email") { value("test@example.com") }
                }
            }
        }
        
        When("존재하지 않는 ID로 요청시") {
            every { userQueryService.getUser(999L) } returns null
            
            val result = mockMvc.get("/api/v1/users/999")
            
            Then("404 Not Found 응답") {
                result.andExpect {
                    status { isNotFound() }
                }
            }
        }
    }
})
```

### 5. Swagger 문서화

#### 태그 설정
```kotlin
@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequestMapping("/api/v1/users")
class UserController
```

#### 상세 문서화
```kotlin
@Operation(
    summary = "사용자 생성",
    description = """
        새로운 사용자를 생성합니다.
        
        **요구사항:**
        - 이메일은 유니크해야 합니다
        - 비밀번호는 8자 이상이어야 합니다
        - 닉네임은 2-20자 사이여야 합니다
    """
)
@ApiResponses(
    ApiResponse(
        responseCode = "201", 
        description = "사용자 생성 성공",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = UserResponse::class)
        )]
    ),
    ApiResponse(
        responseCode = "400", 
        description = "유효하지 않은 요청",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
        )]
    ),
    ApiResponse(responseCode = "409", description = "이메일 중복")
)
```

## Advanced Features

### 1. 필터링과 검색 (--with-filter)
```kotlin
@GetMapping("/search")
@Operation(summary = "사용자 검색")
fun searchUsers(
    @RequestParam(required = false) keyword: String?,
    @RequestParam(required = false) status: UserStatus?,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
    pageable: Pageable
): Page<UserResponse> {
    val filter = UserSearchFilter(
        keyword = keyword,
        status = status,
        startDate = startDate,
        endDate = endDate
    )
    
    return userQueryService.searchUsers(filter, pageable)
        .map { UserResponse.from(it) }
}
```

### 2. 파일 업로드 API
```kotlin
@PostMapping("/{id}/profile-image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
@Operation(summary = "프로필 이미지 업로드")
fun uploadProfileImage(
    @PathVariable id: Long,
    @RequestParam("file") file: MultipartFile
): ResponseEntity<UserResponse> {
    val user = userCommandService.updateProfileImage(id, file)
        ?: return ResponseEntity.notFound().build()
    return ResponseEntity.ok(UserResponse.from(user))
}
```

### 3. 배치 API
```kotlin
@PostMapping("/batch")
@Operation(summary = "사용자 일괄 생성")
fun createUsersBatch(
    @Valid @RequestBody requests: List<CreateUserRequest>
): ResponseEntity<List<UserResponse>> {
    require(requests.size <= 100) { "한 번에 최대 100명까지만 생성 가능합니다" }
    
    val users = userCommandService.createUsersBatch(
        requests.map { it.toCommand() }
    )
    
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(users.map { UserResponse.from(it) })
}
```

### 4. 비동기 API
```kotlin
@PostMapping("/export")
@Operation(summary = "사용자 데이터 내보내기")
fun exportUsers(): ResponseEntity<Map<String, Any>> {
    val jobId = userQueryService.startExportJob()
    
    return ResponseEntity.accepted()
        .body(mapOf(
            "jobId" to jobId,
            "status" to "STARTED",
            "checkUrl" to "/api/v1/jobs/$jobId"
        ))
}
```

## Security Integration

### 인증이 필요한 API
```kotlin
@PostMapping
@PreAuthorize("hasRole('USER')")
@Operation(summary = "사용자 생성 (인증 필요)")
fun createUser(
    @AuthenticationPrincipal userPrincipal: UserPrincipal,
    @Valid @RequestBody request: CreateUserRequest
): ResponseEntity<UserResponse>
```

### 관리자 전용 API
```kotlin
@DeleteMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
@Operation(summary = "사용자 삭제 (관리자 전용)")
fun deleteUser(@PathVariable id: Long)
```

## Performance Considerations

### 캐싱 적용
```kotlin
@GetMapping("/{id}")
@Cacheable(value = ["users"], key = "#id")
@Operation(summary = "사용자 조회 (캐시 적용)")
fun getUser(@PathVariable id: Long): ResponseEntity<UserResponse>
```

### Rate Limiting
```kotlin
@PostMapping
@RateLimit(limit = 10, duration = 60) // 분당 10회
@Operation(summary = "사용자 생성 (Rate Limit 적용)")
fun createUser(@Valid @RequestBody request: CreateUserRequest)
```

## Generated Files Structure

```
src/main/kotlin/com/metamong/
└── presentation/api/user/
    ├── UserController.kt (메서드 추가)
    └── dto/
        ├── CreateUserRequest.kt
        ├── UpdateUserRequest.kt
        └── UserResponse.kt (업데이트)

src/main/kotlin/com/metamong/application/user/
├── command/
│   ├── CreateUserCommand.kt
│   └── UpdateUserCommand.kt
└── service/
    └── UserCommandService.kt (메서드 추가)

src/test/kotlin/com/metamong/
└── presentation/api/user/
    └── UserApiTest.kt (테스트 추가)
```

## Command Options

- `--with-pagination`: 페이징 기능 추가
- `--with-filter`: 검색/필터링 기능 추가
- `--no-auth`: 인증 없이 접근 가능한 API
- `--admin-only`: 관리자 전용 API
- `--async`: 비동기 처리 API
- `--with-cache`: 캐시 적용
- `--with-rate-limit`: Rate Limiting 적용