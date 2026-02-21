# API Design Guidelines

## RESTful URL 규칙

- 리소스: 명사, 복수형, 소문자, 하이픈(-) 구분, 버전 명시 (`/api/v1/`)
- Good: `GET /api/v1/users`, `GET /api/v1/users/{id}/posts`
- Bad: `GET /api/v1/getUsers` (동사), `POST /api/v1/user` (단수)

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

## Controller 규칙

- `@RestController` + `@RequestMapping("/api/v1/{리소스}")` + `@Tag` (Swagger)
- 각 메서드에 `@Operation(summary = "...")` 문서화
- Service 의존성 주입, 비즈니스 로직은 Service에 위임
- POST → `ResponseEntity.created(URI)`, DELETE → `@ResponseStatus(HttpStatus.NO_CONTENT)`

## Request/Response DTO 규칙

- **Request**: `data class` + `@field:` validation + `toDto()` 변환
- **RequestDto**: Request에서 변환된 순수 데이터 클래스 (같은 파일 내 정의)
- **네이밍**: `Create{Domain}RequestDto`, `Update{Domain}RequestDto` (`Command` 접미사 사용 안함)
- **Response**: `data class` + `companion object { fun from(entity/dto) }` 팩토리
- **Response 필드에 `@Schema` 필수** — description, example 명시
- Request/Response는 Presentation 레이어에만 존재

```kotlin
// Response @Schema 예시
data class UserResponse(
    @Schema(description = "사용자 ID", example = "1")
    val id: Long,
    @Schema(description = "이메일", example = "user@example.com")
    val email: String,
)
```

## 에러 처리

- `ApiResponse<T>` 통일 응답 포맷: `success`, `code`(Int), `message`, `data`
- `@ControllerAdvice` + `ApiExceptionHandler`에서 글로벌 예외 처리
- `CustomException` 단일 핸들러로 모든 비즈니스 예외 처리 (`e.status`, `e.message` 활용)
- 비즈니스 예외는 도메인별 `sealed class` 사용 (`UserException.NotFound()` 등)
- HTTP 상태별 예외 클래스(`BadRequestException` 등) 사용 금지
- 에러 메시지에 내부 구현 상세/스택트레이스 노출 금지

## 페이징

- `@PageableDefault(page = 0, size = 20, sort = ["createdAt"], direction = DESC)`
- 응답은 Spring `Page<T>` 또는 커스텀 `PageResponse<T>`

## 코드 패턴 참조

- `docs/patterns/domain-patterns.md` — Controller, DTO, ErrorResponse 구현 예시

## 체크리스트

- [ ] RESTful URL 규칙 준수
- [ ] 적절한 HTTP 상태 코드 반환
- [ ] 모든 입력값 `@Valid` 검증
- [ ] Swagger 문서화 (@Tag, @Operation, @Schema)
- [ ] 에러 응답 통일된 포맷
- [ ] 페이징 처리
