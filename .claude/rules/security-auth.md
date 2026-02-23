---
paths: "**/security/**/*.kt, **/auth/**/*.kt"
---

# Security Guidelines (Auth)

## 인증/인가

- JWT 토큰: Access 1시간, Refresh 7일
- Stateless 세션 (`SessionCreationPolicy.STATELESS`)
- `Authorization: Bearer <token>` 헤더에서 추출
- 시크릿 키는 환경변수/Secrets Manager에서 주입
- Role 기반 접근 제어 (RBAC), `@PreAuthorize` 활용
- API 엔드포인트별 권한 설정
- 토큰 블랙리스트 관리
- 토큰 검증 실패 시 로그 남기되 민감정보 노출 금지

## 코드 패턴 참조

- `docs/patterns/security-patterns.md` — JWT, 인증/인가 구현 예시
