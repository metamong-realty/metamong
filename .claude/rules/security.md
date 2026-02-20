---
globs: "**/security/**/*.kt,**/auth/**/*.kt"
---

# Security Guidelines

## 필수 보안 체크리스트

### 인증/인가
- [ ] JWT 토큰 검증 (Access: 1시간, Refresh: 7일)
- [ ] Role 기반 접근 제어 (RBAC), `@PreAuthorize` 활용
- [ ] API 엔드포인트별 권한 설정
- [ ] 토큰 블랙리스트 관리

### 입력 검증
- [ ] 모든 입력값 검증 (`@Valid`, `@Validated`)
- [ ] SQL Injection 방지 (파라미터 바인딩, QueryDSL 사용)
- [ ] XSS 방지 (HTML 이스케이핑)
- [ ] Path Traversal / Command Injection 방지

### 암호화
- [ ] 비밀번호: BCrypt (`BCryptPasswordEncoder`)
- [ ] 민감정보: AES/GCM 암호화
- [ ] HTTPS 통신 강제
- [ ] 암호화 키: AWS Secrets Manager 등 외부 관리

### 보안 헤더
- [ ] CORS, CSP, X-Frame-Options, X-Content-Type-Options, HSTS

## 핵심 규칙

### JWT
- Stateless 세션 (`SessionCreationPolicy.STATELESS`)
- `Authorization: Bearer <token>` 헤더에서 토큰 추출
- 시크릿 키는 환경변수/Secrets Manager에서 주입 (`@Value`)
- 토큰 검증 실패 시 로그 남기되 민감정보 노출 금지

### 입력값 검증 예시
```kotlin
data class CreateUserRequest(
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    @field:NotBlank(message = "이메일은 필수입니다")
    val email: String,

    @field:Size(min = 2, max = 20, message = "닉네임은 2-20자여야 합니다")
    @field:Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "한글, 영문, 숫자만 가능")
    val nickname: String,

    @field:Size(min = 8, message = "비밀번호는 8자 이상")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]+$",
        message = "영문, 숫자, 특수문자 포함 필수"
    )
    val password: String
)
```

### 권한 제어
```kotlin
@GetMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
fun getAllUsers(): List<UserDto>

@DeleteMapping("/users/{id}")
@PreAuthorize("hasRole('ADMIN') and @userSecurity.isNotSelf(#id)")
fun deleteUser(@PathVariable id: Long)
```

### Rate Limiting
- Redis 기반 `@RateLimit` 어노테이션 + AOP 적용
- IP + 경로 조합 키, 기본값: 분당 10회
- 초과 시 `RateLimitExceededException` 발생

### 보안 로깅
- `@Audited` 어노테이션으로 감사 로그 자동 기록
- 로깅에 민감정보(비밀번호, 토큰 등) 절대 포함 금지

## 코드 리뷰 보안 체크리스트

- [ ] 하드코딩된 시크릿 없음
- [ ] SQL Injection 방지 (QueryDSL/파라미터 바인딩)
- [ ] XSS / CSRF 방지
- [ ] 적절한 인증/인가 적용
- [ ] 모든 입력값 검증
- [ ] 에러 메시지에 민감정보 노출 없음
- [ ] 로깅에 민감정보 없음
- [ ] 암호화 적용 (BCrypt, AES)
- [ ] Rate Limiting 적용
