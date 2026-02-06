# API Design Guidelines

## RESTful API 원칙

### URL 규칙
- 리소스는 명사, 복수형 사용
- 소문자, 하이픈(-) 구분
- 버전 명시 (/api/v1/)
- 계층 구조 표현

```kotlin
// Good
GET    /api/v1/users           // 사용자 목록
GET    /api/v1/users/{id}      // 사용자 조회
POST   /api/v1/users           // 사용자 생성
PUT    /api/v1/users/{id}      // 사용자 수정
DELETE /api/v1/users/{id}      // 사용자 삭제

GET    /api/v1/users/{id}/posts  // 사용자의 게시글

// Bad
GET    /api/v1/getUsers         // 동사 사용
POST   /api/v1/user            // 단수형
GET    /api/v1/users/list      // 불필요한 경로
```

### HTTP 메서드 사용
| 메서드 | 용도 | 멱등성 | 안전성 |
|--------|------|--------|--------|
| GET | 조회 | O | O |
| POST | 생성 | X | X |
| PUT | 전체 수정 | O | X |
| PATCH | 부분 수정 | O | X |
| DELETE | 삭제 | O | X |

### HTTP 상태 코드
```kotlin
// 2xx Success
200 OK                  // 성공
201 Created            // 생성 성공
204 No Content         // 삭제 성공

// 4xx Client Error
400 Bad Request        // 잘못된 요청
401 Unauthorized       // 인증 실패
403 Forbidden          // 권한 없음
404 Not Found          // 리소스 없음
409 Conflict           // 충돌 (중복 등)
422 Unprocessable Entity // 검증 실패

// 5xx Server Error
500 Internal Server Error  // 서버 오류
502 Bad Gateway           // 게이트웨이 오류
503 Service Unavailable   // 서비스 불가
```

## Controller 구현

### 기본 구조
```kotlin
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "사용자 관리 API")
class UserController(
    private val userService: UserService
) {
    
    @GetMapping
    @Operation(summary = "사용자 목록 조회")
    fun getUsers(
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable
    ): ResponseEntity<Page<UserResponse>> {
        val users = userService.getUsers(pageable)
        return ResponseEntity.ok(users.map { UserResponse.from(it) })
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "사용자 상세 조회")
    fun getUser(
        @PathVariable id: Long
    ): ResponseEntity<UserResponse> {
        val user = userService.getUser(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(UserResponse.from(user))
    }
    
    @PostMapping
    @Operation(summary = "사용자 생성")
    fun createUser(
        @Valid @RequestBody request: CreateUserRequest
    ): ResponseEntity<UserResponse> {
        val user = userService.createUser(request.toCommand())
        return ResponseEntity
            .created(URI.create("/api/v1/users/${user.id}"))
            .body(UserResponse.from(user))
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "사용자 수정")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserResponse> {
        val user = userService.updateUser(id, request.toCommand())
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(UserResponse.from(user))
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "사용자 삭제")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(
        @PathVariable id: Long
    ) {
        userService.deleteUser(id)
    }
}
```

## Request/Response DTO

### Request DTO
```kotlin
data class CreateUserRequest(
    @Schema(description = "이메일", example = "user@example.com")
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String,
    
    @Schema(description = "닉네임", example = "홍길동")
    @field:NotBlank(message = "닉네임은 필수입니다")
    @field:Size(min = 2, max = 20, message = "닉네임은 2-20자여야 합니다")
    val nickname: String,
    
    @Schema(description = "비밀번호", example = "password123!")
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

### Response DTO
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
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(user: User) = UserResponse(
            id = user.id,
            email = user.email,
            nickname = user.nickname,
            status = user.status,
            createdAt = user.createdAt
        )
    }
}
```

## 에러 응답

### 통일된 에러 응답
```kotlin
@Schema(description = "에러 응답")
data class ErrorResponse(
    @Schema(description = "에러 코드", example = "USER_NOT_FOUND")
    val code: String,
    
    @Schema(description = "에러 메시지", example = "사용자를 찾을 수 없습니다")
    val message: String,
    
    @Schema(description = "상세 정보")
    val details: Map<String, Any>? = null,
    
    @Schema(description = "타임스탬프", example = "2024-01-01T00:00:00")
    val timestamp: LocalDateTime = LocalDateTime.now()
)
```

### Global Exception Handler
```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {
    
    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFound(e: EntityNotFoundException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            code = "ENTITY_NOT_FOUND",
            message = e.message ?: "엔티티를 찾을 수 없습니다"
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }
    
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = e.bindingResult.fieldErrors.associate {
            it.field to (it.defaultMessage ?: "검증 실패")
        }
        
        val error = ErrorResponse(
            code = "VALIDATION_ERROR",
            message = "입력값 검증에 실패했습니다",
            details = errors
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }
    
    @ExceptionHandler(DuplicateKeyException::class)
    fun handleDuplicateKey(e: DuplicateKeyException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            code = "DUPLICATE_KEY",
            message = "중복된 값입니다"
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error)
    }
    
    @ExceptionHandler(Exception::class)
    fun handleGeneral(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error(e) { "Unexpected error" }
        
        val error = ErrorResponse(
            code = "INTERNAL_ERROR",
            message = "서버 오류가 발생했습니다"
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}
```

## 페이징

### Pageable 사용
```kotlin
@GetMapping
fun getUsers(
    @PageableDefault(
        page = 0,
        size = 20,
        sort = ["createdAt"],
        direction = Sort.Direction.DESC
    ) pageable: Pageable
): Page<UserResponse> {
    return userService.getUsers(pageable)
        .map { UserResponse.from(it) }
}
```

### Page Response
```kotlin
@Schema(description = "페이지 응답")
data class PageResponse<T>(
    @Schema(description = "컨텐츠")
    val content: List<T>,
    
    @Schema(description = "페이지 정보")
    val page: PageInfo
) {
    @Schema(description = "페이지 정보")
    data class PageInfo(
        @Schema(description = "현재 페이지", example = "0")
        val number: Int,
        
        @Schema(description = "페이지 크기", example = "20")
        val size: Int,
        
        @Schema(description = "전체 요소 수", example = "100")
        val totalElements: Long,
        
        @Schema(description = "전체 페이지 수", example = "5")
        val totalPages: Int,
        
        @Schema(description = "첫 페이지 여부", example = "true")
        val first: Boolean,
        
        @Schema(description = "마지막 페이지 여부", example = "false")
        val last: Boolean
    )
    
    companion object {
        fun <T> from(page: Page<T>) = PageResponse(
            content = page.content,
            page = PageInfo(
                number = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                first = page.isFirst,
                last = page.isLast
            )
        )
    }
}
```

## 필터링과 검색

### Query Parameters
```kotlin
@GetMapping
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
    
    return userService.searchUsers(filter, pageable)
        .map { UserResponse.from(it) }
}
```

## Swagger/OpenAPI

### Swagger 설정
```kotlin
@Configuration
class SwaggerConfig {
    
    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Metamong API")
                    .description("Metamong 서비스 API 문서")
                    .version("v1.0.0")
                    .contact(
                        Contact()
                            .name("API Support")
                            .email("support@metamong.com")
                    )
            )
            .servers(
                listOf(
                    Server().url("http://localhost:8080").description("Local"),
                    Server().url("https://api.metamong.com").description("Production")
                )
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        "bearer-jwt",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            )
    }
}
```

### API 문서화
```kotlin
@Tag(name = "Authentication", description = "인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController {
    
    @Operation(
        summary = "로그인",
        description = "이메일과 비밀번호로 로그인합니다"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "로그인 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패")
    )
    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest
    ): TokenResponse {
        // 구현
    }
}
```

## API 버전 관리

### URL Path 버전
```kotlin
@RestController
@RequestMapping("/api/v1/users")  // v1
class UserV1Controller

@RestController
@RequestMapping("/api/v2/users")  // v2
class UserV2Controller
```

### Header 버전
```kotlin
@GetMapping(
    value = ["/api/users"],
    headers = ["API-Version=1"]
)
fun getUsersV1(): List<UserV1Response>

@GetMapping(
    value = ["/api/users"],
    headers = ["API-Version=2"]
)
fun getUsersV2(): List<UserV2Response>
```

## HATEOAS

### Link 추가
```kotlin
data class UserResponse(
    val id: Long,
    val email: String,
    val nickname: String,
    val links: List<Link> = emptyList()
) {
    companion object {
        fun from(user: User) = UserResponse(
            id = user.id,
            email = user.email,
            nickname = user.nickname,
            links = listOf(
                Link(rel = "self", href = "/api/v1/users/${user.id}"),
                Link(rel = "posts", href = "/api/v1/users/${user.id}/posts"),
                Link(rel = "update", href = "/api/v1/users/${user.id}", method = "PUT"),
                Link(rel = "delete", href = "/api/v1/users/${user.id}", method = "DELETE")
            )
        )
    }
}

data class Link(
    val rel: String,
    val href: String,
    val method: String = "GET"
)
```

## API 보안

### Rate Limiting
```kotlin
@GetMapping
@RateLimit(limit = 100, duration = 60)  // 분당 100회
fun getUsers(): List<UserResponse>
```

### API Key 인증
```kotlin
@Component
class ApiKeyAuthFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val apiKey = request.getHeader("X-API-KEY")
        
        if (!isValidApiKey(apiKey)) {
            response.status = HttpStatus.UNAUTHORIZED.value()
            return
        }
        
        filterChain.doFilter(request, response)
    }
}
```

## API 테스트

### MockMvc 테스트
```kotlin
@WebMvcTest(UserController::class)
class UserControllerTest(
    @Autowired val mockMvc: MockMvc,
    @MockkBean val userService: UserService
) : BehaviorSpec({
    
    Given("GET /api/v1/users/{id}") {
        When("존재하는 사용자 ID로 요청하면") {
            val user = User(id = 1L, email = "test@example.com")
            every { userService.getUser(1L) } returns user
            
            val result = mockMvc.get("/api/v1/users/1")
            
            Then("200 OK와 사용자 정보 반환") {
                result.andExpect {
                    status { isOk() }
                    jsonPath("$.id") { value(1) }
                    jsonPath("$.email") { value("test@example.com") }
                }
            }
        }
    }
})
```