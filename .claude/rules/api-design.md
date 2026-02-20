---
globs: "**/controller/**/*.kt,**/presentation/**/*.kt"
---

# API Design Guidelines

## RESTful URL 규칙
- 리소스: 명사, 복수형, 소문자, 하이픈(-) 구분, 버전 명시 (`/api/v1/`)

```
// Good
GET    /api/v1/users              POST   /api/v1/users
GET    /api/v1/users/{id}         PUT    /api/v1/users/{id}
DELETE /api/v1/users/{id}         GET    /api/v1/users/{id}/posts

// Bad
GET /api/v1/getUsers    // 동사 사용
POST /api/v1/user       // 단수형
GET /api/v1/users/list  // 불필요한 경로
```

## HTTP 메서드
| 메서드 | 용도 | 멱등성 | 안전성 |
|--------|------|--------|--------|
| GET | 조회 | O | O |
| POST | 생성 | X | X |
| PUT | 전체 수정 | O | X |
| PATCH | 부분 수정 | O | X |
| DELETE | 삭제 | O | X |

## HTTP 상태 코드
- **2xx**: 200 OK, 201 Created, 204 No Content
- **4xx**: 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict, 422 Unprocessable Entity
- **5xx**: 500 Internal Server Error, 502 Bad Gateway, 503 Service Unavailable

## Controller 구조

```kotlin
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "사용자 관리 API")
class UserController(
    private val userService: UserService,
) {
    @GetMapping
    @Operation(summary = "사용자 목록 조회")
    fun getUsers(
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable,
    ): ResponseEntity<Page<UserResponse>> =
        ResponseEntity.ok(userService.getUsers(pageable).map { UserResponse.from(it) })

    @PostMapping
    @Operation(summary = "사용자 생성")
    fun createUser(
        @Valid @RequestBody request: CreateUserRequest,
    ): ResponseEntity<UserResponse> {
        val user = userService.createUser(request.toCommand())
        return ResponseEntity.created(URI.create("/api/v1/users/${user.id}")).body(UserResponse.from(user))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(@PathVariable id: Long) = userService.deleteUser(id)
}
```

## Request/Response DTO

- Request: `data class` + `@field:` validation + `toCommand()` 변환 메서드
- Response: `data class` + `companion object { fun from(entity) }` 팩토리

```kotlin
// Request
data class CreateUserRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String,
    @field:Size(min = 2, max = 20, message = "닉네임은 2-20자여야 합니다")
    val nickname: String,
) {
    fun toCommand() = CreateUserCommand(email = email, nickname = nickname)
}

// Response
data class UserResponse(
    val id: Long,
    val email: String,
    val nickname: String,
    val status: UserStatus,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(user: User) = UserResponse(
            id = user.id, email = user.email, nickname = user.nickname,
            status = user.status, createdAt = user.createdAt,
        )
    }
}
```

## 에러 응답

통일된 `ErrorResponse` 포맷 + `@RestControllerAdvice`로 글로벌 처리:

```kotlin
data class ErrorResponse(
    val code: String,          // "USER_NOT_FOUND"
    val message: String,       // "사용자를 찾을 수 없습니다"
    val details: Map<String, Any>? = null,
    val timestamp: LocalDateTime = LocalDateTime.now(),
)
```

필수 핸들러: `EntityNotFoundException` -> 404, `MethodArgumentNotValidException` -> 400, `DuplicateKeyException` -> 409, `Exception` -> 500

## 페이징

- `@PageableDefault(page = 0, size = 20, sort = ["createdAt"], direction = DESC)` 사용
- 응답은 Spring `Page<T>` 또는 커스텀 `PageResponse<T>` (content + page info) 사용
