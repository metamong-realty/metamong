---
globs: "**/*.kt"
---

# Kotlin Style Guidelines

## Naming Rules

| 대상 | 규칙 | 예시 |
|------|------|------|
| 클래스/인터페이스 | PascalCase, 약어 금지 | `UserRepository`, `PaymentGateway` |
| 함수 | camelCase, 동사로 시작 | `createUser()`, `findByEmail()` |
| 변수 | camelCase, 명사 | `userEmail`, `postCount` |
| 상수 | UPPER_SNAKE_CASE, companion object 내 선언 | `MAX_RETRY_COUNT` |
| 패키지 | 소문자만, 구분자 없음 | `com.metamong.domain.user` |

## Immutability First

`val` 우선, `var` 최소화. DTO는 `data class` + `val`, Entity는 일반 `class`.

```kotlin
// Good
data class UserDto(val id: Long, val email: String)

// Bad
class UserDto(var id: Long, var email: String)
```

## Null Safety

`!!` 사용 금지. safe call(`?.`)과 Elvis(`?:`) 활용.

```kotlin
// Good
val userName = user?.name ?: "Unknown"
fun findUser(id: Long): User? = userRepository.findByIdOrNull(id)

// Bad - !! 사용 금지
fun findUser(id: Long): User = userRepository.findById(id)!!
```

## Class Structure Order

1. 생성자 파라미터
2. companion object (상수, 로거)
3. 프로퍼티
4. init 블록
5. public 함수
6. internal 함수
7. private 함수
8. 내부 클래스/인터페이스

## when/if Expressions

표현식으로 사용. enum은 모든 케이스 처리하여 `else` 불필요하게 작성.

```kotlin
val message = when (status) {
    Status.SUCCESS -> "성공"
    Status.FAIL -> "실패"
    Status.PENDING -> "대기중"
}

val max = if (a > b) a else b
```

## Exception Handling

```kotlin
// 검증: require (인자), check (상태), checkNotNull (null)
require(age >= 0) { "나이는 0 이상이어야 합니다" }
check(isInitialized) { "초기화되지 않았습니다" }

// 안전한 실행: runCatching
val result = runCatching { riskyOperation() }
    .onFailure { logger.error(it) { "실패" } }
    .getOrNull()
```

## Data Class vs Class

- **DTO/VO**: `data class` 사용
- **JPA Entity**: 일반 `class` 사용 (JPA 제약)

## ktlint

- 저장시 자동 포맷팅, 커밋 전 체크, CI 통합
- `indent_size = 4`, `max_line_length = 120`
