# Security Guidelines (General)

## 입력 검증

- 모든 외부 입력값 `@Valid`, `@Validated` 적용 (Controller, 메시지 핸들러 등)
- SQL Injection 방지: 파라미터 바인딩, QueryDSL 사용 — 문자열 연결로 쿼리 생성 금지
- XSS 방지: 사용자 입력 HTML 이스케이핑, Response에 raw 입력값 반환 금지
- Path Traversal 방지: 파일 경로에 사용자 입력 직접 사용 금지
- Command Injection 방지: `Runtime.exec()`, `ProcessBuilder`에 사용자 입력 전달 금지
- 역직렬화: 신뢰할 수 없는 입력의 `ObjectMapper` 처리 시 타입 제한 설정

## 민감정보 보호

- 비밀번호, 토큰, API 키 등 민감정보 로그 출력 절대 금지
- 에러 응답에 스택트레이스, 내부 구현 상세 노출 금지
- 시크릿/크리덴셜: 코드 하드코딩 금지 → 환경변수 또는 Secrets Manager에서 주입
- 민감 데이터 암호화: 비밀번호 `BCryptPasswordEncoder`, 기타 민감정보 AES/GCM

## API 보안

- Rate Limiting: Redis 기반 `@RateLimit` + AOP, 초과 시 `RateLimitExceededException`
- 보안 헤더: CORS, CSP, X-Frame-Options, X-Content-Type-Options, HSTS
- HTTPS 통신 강제
- 감사 로그: `@Audited` 어노테이션으로 주요 변경 자동 기록

## 보안 체크리스트

- [ ] 하드코딩된 시크릿 없음
- [ ] SQL Injection 방지 (QueryDSL/파라미터 바인딩)
- [ ] XSS / CSRF 방지
- [ ] 모든 입력값 검증
- [ ] 에러 메시지에 민감정보 노출 없음
- [ ] 로깅에 민감정보 없음
- [ ] 암호화 적용 (BCrypt, AES)

## 코드 패턴 참조

- `docs/patterns/security-patterns.md` — 입력 검증, 암호화, Rate Limiting 구현 예시
