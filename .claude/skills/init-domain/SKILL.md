---
description: "새 도메인의 전체 구조를 자동 생성합니다 (Entity, Repository, Service, Controller, DTO, Test)"
disable-model-invocation: true
argument-hint: "<domain-name>"
---

인자로 받은 도메인 이름으로 전체 레이어 구조를 생성하라. `{name}`은 도메인명, `{Name}`은 PascalCase 변환이다.

## 생성할 파일 목록

### 1. Domain Layer

**`domain/{name}/model/{Name}Entity.kt`**
```kotlin
@Entity
@Table(name = "{names}")  // 복수형
class {Name}Entity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    // 도메인 필드들
    @Enumerated(EnumType.STRING)
    val status: {Name}Status = {Name}Status.ACTIVE
) : BaseEntity()
```

**`domain/{name}/model/{Name}Status.kt`**
```kotlin
enum class {Name}Status { ACTIVE, INACTIVE }
```

**`domain/{name}/repository/{Name}Repository.kt`** — 인터페이스만
```kotlin
interface {Name}Repository {
    fun save(entity: {Name}Entity): {Name}Entity
    fun findById(id: Long): {Name}Entity?
    fun deleteById(id: Long)
}
```

### 2. Infra Layer

**`infra/persistence/{name}/{Name}JpaRepository.kt`**
```kotlin
interface {Name}JpaRepository : JpaRepository<{Name}Entity, Long>
```

**`infra/persistence/{name}/{Name}RepositoryImpl.kt`** — Repository 구현
```kotlin
@Repository
class {Name}RepositoryImpl(
    private val jpaRepository: {Name}JpaRepository
) : {Name}Repository { /* 위임 구현 */ }
```

**`infra/persistence/{name}/{Name}QueryRepository.kt`** — QueryDSL 조회
```kotlin
interface {Name}QueryRepository {
    fun search(pageable: Pageable): Page<{Name}Entity>
}
```

### 3. Application Layer

**`application/{name}/service/{Name}CommandService.kt`**
```kotlin
@Service
@Transactional
class {Name}CommandService(private val repository: {Name}Repository) {
    fun create(command: Create{Name}Command): {Name}Dto { /* 생성 */ }
}
```

**`application/{name}/service/{Name}QueryService.kt`**
```kotlin
@Service
@Transactional(readOnly = true)
class {Name}QueryService(private val repository: {Name}Repository) {
    fun get(id: Long): {Name}Dto? { /* 조회 */ }
}
```

**`application/{name}/dto/{Name}Dto.kt`** — `companion object from(entity)` 포함

**`application/{name}/command/Create{Name}Command.kt`**

### 4. Presentation Layer

**`presentation/api/{name}/{Name}Controller.kt`**
```kotlin
@RestController
@RequestMapping("/api/v1/{names}")
@Tag(name = "{Name}", description = "{Name} 관리 API")
class {Name}Controller(
    private val commandService: {Name}CommandService,
    private val queryService: {Name}QueryService
) {
    @PostMapping
    @Operation(summary = "{Name} 생성")
    fun create(@Valid @RequestBody request: Create{Name}Request): ResponseEntity<{Name}Response>

    @GetMapping("/{id}")
    @Operation(summary = "{Name} 조회")
    fun get(@PathVariable id: Long): ResponseEntity<{Name}Response>
}
```

**`presentation/api/{name}/dto/Create{Name}Request.kt`** — @field:NotBlank + toCommand()

**`presentation/api/{name}/dto/{Name}Response.kt`** — `companion object from(dto)` 포함

### 5. Test

**`test/.../application/{name}/service/{Name}CommandServiceTest.kt`** — BehaviorSpec, MockK

**`test/.../presentation/api/{name}/{Name}ControllerTest.kt`** — @WebMvcTest, BehaviorSpec

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
