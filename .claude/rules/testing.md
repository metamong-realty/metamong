# Testing Guidelines

## 커버리지 목표

- **Unit Test**: 80% 이상
- **Integration Test**: 주요 시나리오 100%
- **E2E Test**: Critical Path 100%

## TDD 사이클

RED (실패 테스트) → GREEN (최소 통과 코드) → REFACTOR (개선)

## Kotest BehaviorSpec 규칙

- Given-When-Then 패턴 필수
- 테스트명은 **한글** 사용 필수
- 한 테스트에 하나의 검증

## MockK 규칙

- `mockk<T>()`: 일반 Mock, `mockk<T>(relaxed = true)`: 기본값 반환
- `every { }` 로 명시적 stubbing, relaxed mock 최소화
- `verify(exactly = N) { }` 로 호출 검증

## 테스트 격리

- `beforeEach`에서 데이터 정리
- `afterEach`에서 `clearAllMocks()`
- 통합 테스트: `@Transactional @Rollback` 사용
- 테스트 슬라이스: `@WebMvcTest`, `@DataJpaTest` 활용

## 테스트 명명

- BehaviorSpec: 한글 시나리오 (`Given("사용자가 로그인을 시도할 때")`)
- JUnit 스타일: `` `createUser - 유효한 이메일 - 사용자 생성 성공`() ``

## 코드 패턴 참조

- `docs/patterns/testing-patterns.md` — BehaviorSpec, MockK, Fixture, Matcher 패턴

## 체크리스트

- [ ] Given-When-Then 구조 준수
- [ ] 한 테스트에 하나의 검증
- [ ] 테스트 격리 보장
- [ ] Mock 최소화
- [ ] 의미있는 한글 테스트 이름
- [ ] 엣지 케이스 포함
- [ ] 실패 케이스 테스트
- [ ] 테스트 실행 시간 < 1초
