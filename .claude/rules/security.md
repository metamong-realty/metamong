# Security Guidelines

## 인증/인가

- JWT 토큰: Access 1시간, Refresh 7일
- Stateless 세션 (`SessionCreationPolicy.STATELESS`)
- `Authorization: Bearer <token>` 헤더에서 추출
- 시크릿 키는 환경변수/Secrets Manager에서 주입
- Role 기반 접근 제어 (RBAC), `@PreAuthorize` 활용
- API 엔드포인트별 권한 설정
- 토큰 블랙리스트 관리
- 토큰 검증 실패 시 로그 남기되 민감정보 노출 금지

## 입력 검증

- 모든 입력값 `@Valid`, `@Validated` 적용
- SQL Injection 방지: 파라미터 바인딩, QueryDSL 사용
- XSS 방지: HTML 이스케이핑
- Path Traversal / Command Injection 방지

## 암호화

- 비밀번호: `BCryptPasswordEncoder`
- 민감정보: AES/GCM 암호화
- HTTPS 통신 강제
- 암호화 키: 외부 관리 (AWS Secrets Manager 등)

## 보안 헤더

- CORS, CSP, X-Frame-Options, X-Content-Type-Options, HSTS

## Rate Limiting

- Redis 기반 `@RateLimit` + AOP 적용
- IP + 경로 조합 키, 기본값: 분당 10회
- 초과 시 `RateLimitExceededException` 발생

## 보안 로깅

- 감사 로그: `@Audited` 어노테이션으로 자동 기록
- 민감정보(비밀번호, 토큰 등) 로깅 절대 금지

## 코드 패턴 참조

- `docs/patterns/security-patterns.md` — JWT, 입력 검증, 암호화, Rate Limiting 구현 예시

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
