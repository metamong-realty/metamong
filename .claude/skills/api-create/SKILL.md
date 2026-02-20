---
description: "RESTful API 엔드포인트를 생성합니다 (Controller, DTO, Service 연동, Swagger 문서화)"
disable-model-invocation: true
argument-hint: "<method> <path>"
---

인자로 받은 HTTP method와 path에 해당하는 API 엔드포인트를 생성하라. 기존 Controller가 있으면 메서드를 추가하고, 없으면 새로 생성하라.

## 생성 대상

### Controller Method

method에 따라 아래 템플릿을 적용하라:

**POST** (생성)
```kotlin
@PostMapping
@Operation(summary = "{리소스} 생성")
@ApiResponses(ApiResponse(responseCode = "201"), ApiResponse(responseCode = "400"), ApiResponse(responseCode = "409"))
fun create(@Valid @RequestBody request: CreateRequest): ResponseEntity<Response> {
    val result = commandService.create(request.toCommand())
    return ResponseEntity.created(URI.create("{path}/${result.id}")).body(Response.from(result))
}
```

**GET** (단건 조회)
```kotlin
@GetMapping("/{id}")
@Operation(summary = "{리소스} 조회")
fun get(@PathVariable id: Long): ResponseEntity<Response> {
    val result = queryService.get(id) ?: return ResponseEntity.notFound().build()
    return ResponseEntity.ok(Response.from(result))
}
```

**GET** (목록 + 페이징)
```kotlin
@GetMapping
@Operation(summary = "{리소스} 목록 조회")
fun list(
    @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    @RequestParam(required = false) keyword: String?,
    @RequestParam(required = false) status: Status?
): ResponseEntity<Page<Response>> {
    return ResponseEntity.ok(queryService.search(keyword, status, pageable).map { Response.from(it) })
}
```

**PUT** (수정)
```kotlin
@PutMapping("/{id}")
@Operation(summary = "{리소스} 수정")
fun update(@PathVariable id: Long, @Valid @RequestBody request: UpdateRequest): ResponseEntity<Response> {
    val result = commandService.update(id, request.toCommand()) ?: return ResponseEntity.notFound().build()
    return ResponseEntity.ok(Response.from(result))
}
```

**DELETE** (삭제)
```kotlin
@DeleteMapping("/{id}")
@Operation(summary = "{리소스} 삭제")
@ResponseStatus(HttpStatus.NO_CONTENT)
fun delete(@PathVariable id: Long) { commandService.delete(id) }
```

### Request/Response DTO

- **CreateRequest**: `@field:NotBlank`, `@field:Email`, `@field:Size` 등 검증 + `toCommand()` 변환 메서드
- **UpdateRequest**: nullable 필드 + `toCommand()` 변환
- **Response**: `companion object { fun from(dto) }` 패턴, `@Schema` 문서화

### Service 연동

해당 Service에 메서드가 없으면 추가하라:
- CommandService: create, update, delete (`@Transactional`)
- QueryService: get, search (`@Transactional(readOnly = true)`)
- Command 객체로 레이어 간 데이터 전달

### Controller 테스트

```kotlin
@WebMvcTest(Controller::class)
class ApiTest(
    @Autowired val mockMvc: MockMvc,
    @MockkBean val commandService: CommandService,
    @MockkBean val queryService: QueryService
) : BehaviorSpec({
    Given("{METHOD} {path}") {
        When("유효한 요청시") { Then("성공 응답") { /* 검증 */ } }
        When("유효하지 않은 요청시") { Then("에러 응답") { /* 검증 */ } }
    }
})
```

## 옵션 처리

- `--with-pagination`: 목록 조회에 Pageable 추가
- `--with-filter`: 검색 파라미터(keyword, status, date range) 추가
- `--no-auth`: 인증 불필요 엔드포인트
- `--admin-only`: `@PreAuthorize("hasRole('ADMIN')")` 추가
- `--with-cache`: `@Cacheable` 적용
- `--with-rate-limit`: `@RateLimit` 적용
