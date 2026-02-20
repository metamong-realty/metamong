---
globs: "**/*Test*.kt,**/*Spec*.kt,**/test/**/*.kt"
---

# Testing Guidelines

## 커버리지 목표
- **Unit Test**: 80% 이상
- **Integration Test**: 주요 시나리오 100%
- **E2E Test**: Critical Path 100%

## TDD 사이클
RED (실패 테스트) -> GREEN (최소 통과 코드) -> REFACTOR (개선)

## Kotest BehaviorSpec 구조

Given-When-Then 패턴으로 작성. 테스트명은 **한글** 사용 필수.

```kotlin
class UserServiceTest : BehaviorSpec({
    val mockUserRepository = mockk<UserRepository>()
    val userService = UserService(userRepository = mockUserRepository)

    Given("사용자 생성 요청이 있을 때") {
        val request = CreateUserRequest(email = "test@example.com", nickname = "테스터")

        When("유효한 데이터로 요청하면") {
            every { mockUserRepository.save(any()) } returns User(id = 1L, email = "test@example.com")
            val result = userService.createUser(request)

            Then("사용자가 생성된다") {
                result shouldNotBe null
                result.email shouldBe "test@example.com"
            }
        }

        When("이미 존재하는 이메일로 요청하면") {
            every { mockUserRepository.existsByEmail(any()) } returns true

            Then("예외가 발생한다") {
                shouldThrow<DuplicateEmailException> { userService.createUser(request) }
            }
        }
    }
})
```

## MockK 핵심

```kotlin
// 생성
val mock = mockk<UserService>()              // 일반 Mock
val relaxed = mockk<UserService>(relaxed = true)  // 기본값 반환

// Stubbing
every { mock.findById(1L) } returns user
every { mock.save(any()) } throws RuntimeException("DB Error")
every { mock.sendEmail(any()) } just Runs

// Verification
verify(exactly = 1) { mock.save(any()) }
verify(exactly = 0) { mock.delete(any()) }
verify { mock.save(match { it.email == "test@example.com" }) }
```

## 테스트 격리
- `beforeEach`에서 데이터 정리, `afterEach`에서 `clearAllMocks()`
- 통합 테스트: `@Transactional @Rollback` 사용
- 테스트 슬라이스: `@WebMvcTest`, `@DataJpaTest` 활용

## 테스트 명명 규칙

BehaviorSpec은 한글로 시나리오 작성:
```
Given("사용자가 로그인을 시도할 때")
When("올바른 비밀번호를 입력하면")
Then("로그인에 성공한다")
```

JUnit 스타일: `` `createUser - 유효한 이메일 - 사용자 생성 성공`() ``

## 테스트 체크리스트

- [ ] Given-When-Then 구조 준수
- [ ] 한 테스트에 하나의 검증
- [ ] 테스트 격리 보장
- [ ] Mock 최소화
- [ ] 의미있는 한글 테스트 이름
- [ ] 엣지 케이스 포함
- [ ] 실패 케이스 테스트
- [ ] 테스트 실행 시간 < 1초
