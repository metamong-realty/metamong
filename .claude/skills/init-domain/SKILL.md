---
description: "새 도메인의 전체 구조를 자동 생성합니다 (Entity, Repository, Service, Controller, DTO, Test)"
disable-model-invocation: true
argument-hint: "<domain-name>"
---

인자로 받은 도메인 이름으로 전체 레이어 구조를 생성하라. `{name}`은 도메인명, `{Name}`은 PascalCase 변환이다.

## 생성할 파일 목록

### 1. Domain Layer
- `domain/{name}/model/{Name}Entity.kt` — Entity, BaseEntity 상속
- `domain/{name}/model/{Name}Status.kt` — enum class (ACTIVE, INACTIVE)
- `domain/{name}/repository/{Name}Repository.kt` — 인터페이스만 (save, findById, deleteById)

### 2. Infra Layer
- `infra/persistence/{name}/{Name}JpaRepository.kt` — JpaRepository 상속
- `infra/persistence/{name}/{Name}RepositoryImpl.kt` — Repository 구현 (위임)
- `infra/persistence/{name}/{Name}QueryRepository.kt` — QueryDSL 조회 인터페이스

### 3. Application Layer
- `application/{name}/service/{Name}CommandService.kt` — `@Transactional`, create
- `application/{name}/service/{Name}QueryService.kt` — `@Transactional(readOnly = true)`, get
- `application/{name}/dto/{Name}Dto.kt` — `companion object from(entity)` 포함
- `application/{name}/command/Create{Name}Command.kt`

### 4. Presentation Layer
- `presentation/api/{name}/{Name}Controller.kt` — REST 엔드포인트, Swagger 문서화
- `presentation/api/{name}/dto/Create{Name}Request.kt` — `@field:` 검증 + `toCommand()`
- `presentation/api/{name}/dto/{Name}Response.kt` — `companion object from(dto)` 포함

### 5. Test
- `test/.../application/{name}/service/{Name}CommandServiceTest.kt` — BehaviorSpec, MockK
- `test/.../presentation/api/{name}/{Name}ControllerTest.kt` — @WebMvcTest, BehaviorSpec

## 코드 패턴 참조

각 레이어의 코드 패턴은 아래 파일을 참조하라:
- `docs/patterns/domain-patterns.md` — Entity, VO, Repository 패턴
- `docs/patterns/querydsl-patterns.md` — QueryDSL 조회 패턴
- `docs/patterns/testing-patterns.md` — 테스트 구조 패턴

## 규칙

- Entity: 연관관계 매핑 금지, `@Enumerated(EnumType.STRING)` 필수, Setter 금지
- Repository: Domain에 인터페이스, Infra에 구현
- Service: Command/Query 분리
- Test: 한글 시나리오명, Given-When-Then

## 옵션 처리

사용자가 옵션을 지정하면 아래를 적용하라:
- `--with-audit`: BaseAuditEntity 상속 (createdBy, updatedBy)
- `--with-soft-delete`: `@SQLDelete` + `@Where(clause = "deleted_at IS NULL")` 추가
- `--parent <name>`: 부모 도메인 ID 필드(`{parent}Id: Long`) 추가
