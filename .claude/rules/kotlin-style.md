# Kotlin Style Guidelines

## 네이밍 규칙

### 클래스와 인터페이스
- PascalCase 사용
- 명확하고 의미있는 이름
- 약어 사용 금지

```kotlin
// Good
class UserRepository
interface PaymentGateway
data class CreateUserRequest

// Bad
class UserRepo  // 약어
class usr       // 불명확
interface PG    // 약어
```

### 함수와 변수
- camelCase 사용
- 동사로 시작 (함수)
- 명사 사용 (변수)

```kotlin
// Good
fun createUser(email: String): User
val userEmail = "test@example.com"

// Bad
fun user_create()  // snake_case
val UserEmail      // PascalCase
```

### 상수
- UPPER_SNAKE_CASE
- companion object 내부 선언

```kotlin
companion object {
    const val MAX_RETRY_COUNT = 3
    const val DEFAULT_TIMEOUT = 30_000L
}
```

### 패키지
- 소문자만 사용
- 단어 구분 없음

```kotlin
// Good
package com.metamong.domain.user

// Bad
package com.metamong.domain.User
package com.metamong.domain.user_management
```

## 코드 스타일

### 불변성 우선
```kotlin
// Good
val userName: String = "John"
data class User(val id: Long, val name: String)

// Bad
var userName: String = "John"
class User(var id: Long, var name: String)
```

### Null Safety
```kotlin
// Good
fun findUser(id: Long): User? {
    return userRepository.findByIdOrNull(id)
}

val userName = user?.name ?: "Unknown"
val length = user?.name?.length ?: 0

// Bad
fun findUser(id: Long): User {
    return userRepository.findById(id)!!  // !! 사용 금지
}
```

### 함수형 프로그래밍
```kotlin
// Good
val activeUsers = users
    .filter { it.isActive }
    .map { it.toDto() }
    .sortedBy { it.createdAt }

// 람다 파라미터 명시
users.filter { user -> user.age > 18 }

// 단일 람다 파라미터는 it 사용
users.filter { it.age > 18 }
```

### Extension Functions
```kotlin
// 도메인 로직에 유용한 확장 함수
fun String.toEmail(): Email = Email(this)

fun User.isAdult(): Boolean = this.age >= 18

fun List<User>.filterActive(): List<User> = 
    this.filter { it.status == UserStatus.ACTIVE }
```

### Data Class 활용
```kotlin
// DTO에는 data class 사용
data class UserDto(
    val id: Long,
    val email: String,
    val nickname: String
)

// Entity에는 일반 class 사용 (JPA 제약)
@Entity
class User(
    @Id
    val id: Long,
    val email: String
)
```

## 클래스 구조

### 순서 규칙
```kotlin
class UserService(
    private val userRepository: UserRepository  // 1. 생성자 파라미터
) {
    // 2. companion object (상수, 로거)
    companion object {
        private val logger = KotlinLogging.logger {}
        const val MAX_LOGIN_ATTEMPTS = 5
    }
    
    // 3. 프로퍼티
    private val cache = mutableMapOf<Long, User>()
    
    // 4. init 블록
    init {
        logger.info { "UserService initialized" }
    }
    
    // 5. public 함수
    fun createUser(email: String): User {
        // 구현
    }
    
    // 6. internal 함수
    internal fun validateEmail(email: String) {
        // 구현
    }
    
    // 7. private 함수
    private fun encodePassword(password: String): String {
        // 구현
    }
    
    // 8. 내부 클래스/인터페이스
    data class UserCreatedEvent(val userId: Long)
}
```

## 조건문과 반복문

### when 표현식
```kotlin
// Good - 표현식으로 사용
val message = when (status) {
    Status.SUCCESS -> "성공"
    Status.FAIL -> "실패"
    Status.PENDING -> "대기중"
}

// enum 완전성 검사
fun processStatus(status: Status) = when (status) {
    Status.SUCCESS -> handleSuccess()
    Status.FAIL -> handleFail()
    Status.PENDING -> handlePending()
    // else 불필요 - 모든 케이스 처리
}
```

### if 표현식
```kotlin
// Good - 표현식으로 사용
val max = if (a > b) a else b

val status = if (user.isActive) {
    "활성"
} else {
    "비활성"
}

// Elvis operator 활용
val name = user?.name ?: "Unknown"
```

### 반복문
```kotlin
// forEach 선호
users.forEach { user ->
    processUser(user)
}

// 인덱스 필요시
users.forEachIndexed { index, user ->
    println("$index: ${user.name}")
}

// map, filter, reduce 활용
val names = users.map { it.name }
val adults = users.filter { it.age >= 18 }
val totalAge = users.sumOf { it.age }
```

## 예외 처리

### 예외 타입
```kotlin
// 검증 실패
require(age >= 0) { "나이는 0 이상이어야 합니다" }

// 상태 확인
check(isInitialized) { "초기화되지 않았습니다" }

// null 체크
checkNotNull(user) { "사용자를 찾을 수 없습니다" }

// 커스텀 예외
class UserNotFoundException(userId: Long) : 
    RuntimeException("사용자를 찾을 수 없습니다: $userId")
```

### try-catch
```kotlin
// Good
fun parseJson(json: String): User? {
    return try {
        objectMapper.readValue(json, User::class.java)
    } catch (e: JsonProcessingException) {
        logger.error { "JSON 파싱 실패: ${e.message}" }
        null
    }
}

// runCatching 활용
val result = runCatching {
    riskyOperation()
}.onSuccess {
    logger.info { "성공: $it" }
}.onFailure {
    logger.error(it) { "실패" }
}.getOrNull()
```

## 코루틴 스타일

### suspend 함수
```kotlin
suspend fun fetchUser(id: Long): User {
    return withContext(Dispatchers.IO) {
        userRepository.findById(id)
    }
}
```

### async/await
```kotlin
suspend fun fetchUserData(userId: Long): UserData {
    return coroutineScope {
        val user = async { fetchUser(userId) }
        val posts = async { fetchUserPosts(userId) }
        val comments = async { fetchUserComments(userId) }
        
        UserData(
            user = user.await(),
            posts = posts.await(),
            comments = comments.await()
        )
    }
}
```

## 테스트 코드 스타일

### 테스트 함수명
```kotlin
// 한글로 명확하게 작성
@Test
fun `사용자 생성시 이메일 중복 검사를 수행한다`() {
    // given
    val email = "test@example.com"
    
    // when
    val result = userService.createUser(email)
    
    // then
    result shouldNotBe null
}
```

## 주석 규칙

### 필요한 경우만 작성
```kotlin
// Good - 복잡한 비즈니스 로직 설명
/**
 * 사용자 포인트 계산
 * - 기본 포인트: 구매액의 1%
 * - VIP 추가: 2%
 * - 이벤트 기간: 2배
 */
fun calculatePoints(amount: Int, user: User): Int {
    // 구현
}

// Bad - 명백한 코드 설명
// 사용자를 생성한다
fun createUser(email: String) {
    // 구현
}
```

## Import 규칙

### 순서
1. Kotlin 표준 라이브러리
2. Java 표준 라이브러리
3. 서드파티 라이브러리
4. 프로젝트 내부 패키지

```kotlin
import kotlin.collections.List
import java.util.UUID
import org.springframework.stereotype.Service
import com.metamong.domain.user.User
```

### 와일드카드 import
- 5개 이상 동일 패키지에서 import시 사용
- IDE 설정으로 자동화

## ktlint 설정

### 자동 실행
- 저장시 자동 포맷팅
- 커밋 전 체크
- CI/CD 파이프라인 통합

### 커스텀 규칙
```kotlin
// .editorconfig
[*.{kt,kts}]
indent_size = 4
max_line_length = 120
insert_final_newline = true
```