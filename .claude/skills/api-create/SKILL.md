---
description: "RESTful API 엔드포인트를 생성합니다 (Controller, DTO, Service 연동, Swagger 문서화). 사용자가 'API 만들어줘', '엔드포인트 추가', 'POST /api/v1/...' 형태로 API 생성을 요청할 때 사용합니다."
disable-model-invocation: true
argument-hint: "<method> <path>"
---

인자로 받은 HTTP method와 path에 해당하는 API 엔드포인트를 생성하라. 기존 Controller가 있으면 메서드를 추가하고, 없으면 새로 생성하라.

## 생성 대상

### Controller Method

method에 따라 아래 패턴을 적용하라:

- **POST**: 생성 → `201 Created` + Location 헤더 + Response body
- **GET (단건)**: `/{id}` 조회 → `200 OK` 또는 `404 Not Found`
- **GET (목록)**: 페이징 조회 → `@PageableDefault` + `Page<Response>`
- **PUT**: `/{id}` 수정 → `200 OK` 또는 `404 Not Found`
- **DELETE**: `/{id}` 삭제 → `204 No Content`

### Request/Response DTO

- **CreateRequest**: `@field:` 검증 어노테이션 + `toCommand()` 변환 메서드
- **UpdateRequest**: nullable 필드 + `toCommand()` 변환
- **Response**: `companion object { fun from(dto) }` 패턴, `@Schema` 문서화

### Service 연동

해당 Service에 메서드가 없으면 추가하라:
- CommandService: create, update, delete (`@Transactional`)
- QueryService: get, search (`@Transactional(readOnly = true)`)
- Command 객체로 레이어 간 데이터 전달

### Controller 테스트

- `@WebMvcTest` + BehaviorSpec으로 작성
- 유효한 요청 / 유효하지 않은 요청 시나리오 포함

## 코드 패턴 참조

- `docs/patterns/domain-patterns.md` — DTO, Command 패턴
- `docs/patterns/testing-patterns.md` — Controller 테스트 패턴

## 옵션 처리

- `--with-pagination`: 목록 조회에 Pageable 추가
- `--with-filter`: 검색 파라미터(keyword, status, date range) 추가
- `--no-auth`: 인증 불필요 엔드포인트
- `--admin-only`: `@PreAuthorize("hasRole('ADMIN')")` 추가
- `--with-cache`: `@Cacheable` 적용
- `--with-rate-limit`: `@RateLimit` 적용
