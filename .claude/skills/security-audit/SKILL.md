---
description: "프로젝트의 보안 취약점을 OWASP Top 10 기준으로 스캔하고 수정 방안을 제안합니다. 사용자가 '보안 점검', '보안 감사', '취약점 스캔'을 요청하거나, 배포 전 보안 상태를 확인하고 싶을 때 사용합니다."
---

프로젝트의 보안 취약점을 OWASP Top 10 기준으로 스캔하라. 모든 소스 코드와 설정 파일을 분석하라.

## 검사 항목

### A01 — Broken Access Control
- `@PreAuthorize` 누락된 엔드포인트를 찾아라
- IDOR(다른 사용자 리소스 접근) 취약점을 확인하라
- JWT 토큰 검증 로직을 검증하라

### A02 — Cryptographic Failures
- 하드코딩된 Secret Key를 탐지하라 (application.yml, 소스 코드)
- 약한 해싱 (MD5, SHA1)을 찾아라 → BCrypt 사용 확인
- HTTPS 강제 여부를 확인하라

### A03 — Injection
- @Query 사용 또는 문자열 연결 쿼리를 찾아라 → QueryDSL/파라미터 바인딩 확인
- `Runtime.exec()` 등 Command Injection 위험을 탐지하라
- MongoDB 쿼리의 미검증 입력값을 확인하라

### A04 — Insecure Design
- Rate Limiting 부재를 확인하라 (특히 인증 엔드포인트)
- 브루트 포스 공격 방지 로직을 검증하라

### A05 — Security Misconfiguration
- 프로덕션 프로파일의 Debug 모드를 확인하라
- 기본 크리덴셜(admin/admin)을 탐지하라
- CORS `allowedOrigins("*")`를 찾아라
- 불필요한 기능(H2 Console 등) 활성화를 확인하라

## 결과 형식

각 발견 사항을 아래 형식으로 보고하라:

```
CRITICAL: [문제 설명]
   File: 파일명:라인번호
   Code: 문제가 되는 코드
   Fix: 수정 방법 (코드 포함)

HIGH: ...
MEDIUM: ...
PASS: [통과한 항목]
```

## 수정 패턴 참조

- `docs/patterns/security-patterns.md` — Access Control, Injection 방지, 암호화 패턴

## 완료 후

발견된 CRITICAL/HIGH 이슈가 있으면 즉시 수정을 제안하라. 사용자에게 수정 여부를 확인받아라.
