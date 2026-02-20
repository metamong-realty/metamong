---
description: "Test-Driven Development 워크플로를 구현합니다 (RED-GREEN-REFACTOR 사이클)"
disable-model-invocation: true
argument-hint: "<feature-description>"
---

인자로 받은 기능을 TDD 방식(RED → GREEN → REFACTOR)으로 구현하라.

## Phase 1: RED — 실패하는 테스트 작성

1. 요구사항을 분석하고 테스트 시나리오를 정의하라.
2. 아래 구조로 **단위 테스트**를 먼저 작성하라:

```kotlin
class {Feature}Test : BehaviorSpec({
    val mockRepository = mockk<Repository>()
    val service = Service(repository = mockRepository)

    Given("정상 요청이 있을 때") {
        every { mockRepository.save(any()) } returns fixture<Entity>()

        When("기능을 수행하면") {
            val result = service.execute(request)

            Then("기대 결과가 반환된다") {
                result shouldNotBe null
            }
        }
    }

    Given("예외 상황일 때") {
        When("기능을 수행하면") {
            Then("적절한 예외가 발생한다") {
                shouldThrow<CustomException> { service.execute(invalidRequest) }
            }
        }
    }
})
```

3. **API 테스트**도 작성하라:

```kotlin
@WebMvcTest(Controller::class)
class {Feature}ApiTest(
    @Autowired val mockMvc: MockMvc,
    @MockkBean val service: Service
) : BehaviorSpec({
    Given("POST /api/v1/resource") {
        When("유효한 요청시") {
            Then("201 Created") { /* status, jsonPath 검증 */ }
        }
        When("유효하지 않은 요청시") {
            Then("400 Bad Request") { /* 검증 에러 확인 */ }
        }
    }
})
```

## Phase 2: GREEN — 테스트를 통과시키는 최소 코드 작성

- Entity → Service → Controller 순서로 구현하라.
- **테스트를 통과하는 최소한의 코드만** 작성하라.
- 프로젝트 규칙을 준수하라: DDD, Clean Architecture, @Query 금지.

## Phase 3: REFACTOR — 코드 개선

- 비즈니스 로직을 분리하라 (Validator, EventHandler 등).
- 이벤트 기반 처리가 적절하면 `@TransactionalEventListener(phase = AFTER_COMMIT)`를 적용하라.
- 코드 중복을 제거하고 가독성을 개선하라.

## Phase 4: 통합 테스트

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class {Feature}IntegrationTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val repository: Repository
) : BehaviorSpec({
    beforeEach { repository.deleteAll() }

    Given("실제 DB 환경에서") {
        When("전체 플로우를 실행하면") {
            Then("DB에 데이터가 올바르게 저장된다") { /* 검증 */ }
        }
    }
})
```

## 원칙

- 테스트명은 **한글** Given-When-Then
- beforeEach에서 `clearAllMocks()` / 데이터 정리
- 한 테스트에 하나의 검증
- 완료 후 `./gradlew jacocoTestReport`로 커버리지 확인 (Line 80%, Branch 70%)
