---
description: "Test-Driven Development 워크플로를 구현합니다 (RED-GREEN-REFACTOR 사이클)"
disable-model-invocation: true
argument-hint: "<feature-description>"
---

인자로 받은 기능을 TDD 방식(RED → GREEN → REFACTOR)으로 구현하라.

## Phase 1: RED — 실패하는 테스트 작성

1. 요구사항을 분석하고 테스트 시나리오를 정의하라.
2. **단위 테스트**를 먼저 작성하라: BehaviorSpec, MockK, Given-When-Then, 한글 시나리오명
3. **API 테스트**도 작성하라: @WebMvcTest, BehaviorSpec

## Phase 2: GREEN — 테스트를 통과시키는 최소 코드 작성

- Entity → Service → Controller 순서로 구현하라.
- **테스트를 통과하는 최소한의 코드만** 작성하라.
- 프로젝트 규칙을 준수하라: DDD, Clean Architecture, @Query 금지.

## Phase 3: REFACTOR — 코드 개선

- 비즈니스 로직을 분리하라 (Validator, EventHandler 등).
- 이벤트 기반 처리가 적절하면 `@TransactionalEventListener(phase = AFTER_COMMIT)`를 적용하라.
- 코드 중복을 제거하고 가독성을 개선하라.

## Phase 4: 통합 테스트

- `@SpringBootTest` + `@AutoConfigureMockMvc` + `@Transactional`로 실제 DB 환경 테스트
- beforeEach에서 데이터 정리

## 코드 패턴 참조

- `docs/patterns/testing-patterns.md` — BehaviorSpec, MockK, Fixture 패턴
- `docs/patterns/domain-patterns.md` — Entity, Service 구현 패턴

## 원칙

- 테스트명은 **한글** Given-When-Then
- beforeEach에서 `clearAllMocks()` / 데이터 정리
- 한 테스트에 하나의 검증
- 완료 후 `./gradlew jacocoTestReport`로 커버리지 확인 (Line 80%, Branch 70%)
