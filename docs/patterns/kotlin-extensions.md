---
skill: Kotlin 확장 함수 라이브러리
category: Language
patterns: Extensions, DSL, Functional Programming
---

# Kotlin Extensions Skill

Kotlin의 강력한 확장 함수 기능을 활용한 재사용 가능한 유틸리티 라이브러리입니다.

## 컬렉션 확장

### 리스트 변환 및 필터링
```kotlin
// 안전한 변환과 필터링
fun <T, R> List<T>.mapNotNullIndexed(transform: (index: Int, T) -> R?): List<R> =
    mapIndexedNotNull(transform)

fun <T> List<T>.filterNotNullAndDistinct(): List<T> =
    filterNotNull().distinct()

fun <T> List<T>.chunkedBy(size: Int): List<List<T>> =
    chunked(size)

// 조건부 처리
fun <T> List<T>.takeWhileIndexed(predicate: (index: Int, T) -> Boolean): List<T> {
    var index = 0
    return takeWhile { predicate(index++, it) }
}

fun <T> List<T>.dropWhileIndexed(predicate: (index: Int, T) -> Boolean): List<T> {
    var index = 0
    return dropWhile { predicate(index++, it) }
}

// 집계 확장
fun <T> List<T>.secondOrNull(): T? = if (size >= 2) this[1] else null
fun <T> List<T>.thirdOrNull(): T? = if (size >= 3) this[2] else null

fun <T> List<T>.safeTake(count: Int): List<T> = take(maxOf(0, minOf(count, size)))

// 부분 리스트 안전 처리
fun <T> List<T>.safeSubList(fromIndex: Int, toIndex: Int = size): List<T> {
    val safeFrom = maxOf(0, minOf(fromIndex, size))
    val safeTo = maxOf(safeFrom, minOf(toIndex, size))
    return subList(safeFrom, safeTo)
}
```

### Map 확장
```kotlin
// 안전한 Map 접근
fun <K, V> Map<K, V>.getOrDefault(key: K, defaultValue: V): V =
    this[key] ?: defaultValue

fun <K, V> Map<K, V>.getOrCompute(key: K, defaultValue: () -> V): V =
    this[key] ?: defaultValue()

// Map 변환
fun <K, V, R> Map<K, V>.mapValuesNotNull(transform: (Map.Entry<K, V>) -> R?): Map<K, R> =
    mapNotNull { entry ->
        transform(entry)?.let { entry.key to it }
    }.toMap()

fun <K, V> Map<K, V>.filterNotNullValues(): Map<K, V> =
    filterValues { it != null }

// Map 병합
fun <K, V> Map<K, V>.mergeWith(
    other: Map<K, V>,
    merger: (V, V) -> V
): Map<K, V> {
    val result = this.toMutableMap()
    other.forEach { (key, value) ->
        result[key] = result[key]?.let { merger(it, value) } ?: value
    }
    return result
}
```

## 문자열 확장

### 검증 및 변환
```kotlin
// 이메일 검증
fun String.isValidEmail(): Boolean =
    matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))

// 전화번호 정규화
fun String.normalizePhoneNumber(): String =
    replace(Regex("[^0-9]"), "")
        .let { normalized ->
            when {
                normalized.startsWith("010") -> normalized
                normalized.startsWith("82") -> normalized.drop(2)
                else -> normalized
            }
        }

// 한글 검증
fun String.containsKorean(): Boolean =
    matches(Regex(".*[ㄱ-ㅎㅏ-ㅣ가-힣].*"))

fun String.isKoreanOnly(): Boolean =
    matches(Regex("[ㄱ-ㅎㅏ-ㅣ가-힣\\s]*"))

// 케이스 변환
fun String.toSnakeCase(): String =
    replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()

fun String.toCamelCase(): String =
    split("_", "-", " ")
        .mapIndexed { index, word ->
            if (index == 0) word.lowercase()
            else word.lowercase().replaceFirstChar { it.uppercase() }
        }
        .joinToString("")

fun String.toPascalCase(): String =
    split("_", "-", " ")
        .joinToString("") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
```

### 문자열 처리 유틸리티
```kotlin
// 안전한 부분 문자열
fun String.safeSubstring(startIndex: Int, endIndex: Int = length): String {
    val safeStart = maxOf(0, minOf(startIndex, length))
    val safeEnd = maxOf(safeStart, minOf(endIndex, length))
    return substring(safeStart, safeEnd)
}

// 마스킹
fun String.maskEmail(): String =
    if (contains("@")) {
        val parts = split("@")
        val local = parts[0]
        val domain = parts[1]
        "${local.take(2)}${"*".repeat(maxOf(1, local.length - 2))}@$domain"
    } else this

fun String.maskPhoneNumber(): String =
    when {
        length >= 11 -> "${take(3)}-****-${takeLast(4)}"
        length >= 8 -> "${take(3)}-***-${takeLast(3)}"
        else -> this
    }

// 텍스트 정리
fun String.normalizeWhitespace(): String =
    replace(Regex("\\s+"), " ").trim()

fun String.removeEmoji(): String =
    replace(Regex("\\p{So}"), "")

// 줄임 처리
fun String.ellipsize(maxLength: Int, suffix: String = "..."): String =
    if (length <= maxLength) this
    else "${take(maxLength - suffix.length)}$suffix"
```

## 날짜/시간 확장

### LocalDateTime 확장
```kotlin
// 시간대 변환
fun LocalDateTime.toEpochMilli(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun LocalDateTime.toKoreanTime(): LocalDateTime =
    atZone(ZoneId.systemDefault())
        .withZoneSameInstant(ZoneId.of("Asia/Seoul"))
        .toLocalDateTime()

// 날짜 비교
fun LocalDateTime.isSameDay(other: LocalDateTime): Boolean =
    toLocalDate() == other.toLocalDate()

fun LocalDateTime.isToday(): Boolean =
    isSameDay(LocalDateTime.now())

fun LocalDateTime.isYesterday(): Boolean =
    isSameDay(LocalDateTime.now().minusDays(1))

fun LocalDateTime.isTomorrow(): Boolean =
    isSameDay(LocalDateTime.now().plusDays(1))

// 기간 계산
fun LocalDateTime.daysBetween(other: LocalDateTime): Long =
    ChronoUnit.DAYS.between(this.toLocalDate(), other.toLocalDate())

fun LocalDateTime.hoursBetween(other: LocalDateTime): Long =
    ChronoUnit.HOURS.between(this, other)

// 포맷팅
fun LocalDateTime.formatKorean(): String =
    format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분"))

fun LocalDateTime.formatIso(): String =
    format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
```

### LocalDate 확장
```kotlin
// 날짜 검증
fun LocalDate.isWeekend(): Boolean =
    dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

fun LocalDate.isWeekday(): Boolean = !isWeekend()

fun LocalDate.isHoliday(): Boolean {
    // 한국 공휴일 체크 로직 (간단한 예시)
    val month = monthValue
    val day = dayOfMonth
    
    return when {
        month == 1 && day == 1 -> true  // 신정
        month == 3 && day == 1 -> true  // 삼일절
        month == 5 && day == 5 -> true  // 어린이날
        month == 6 && day == 6 -> true  // 현충일
        month == 8 && day == 15 -> true // 광복절
        month == 10 && day == 3 -> true // 개천절
        month == 10 && day == 9 -> true // 한글날
        month == 12 && day == 25 -> true // 성탄절
        else -> false
    }
}

// 기간 계산
fun LocalDate.age(): Int =
    Period.between(this, LocalDate.now()).years

fun LocalDate.startOfWeek(): LocalDate =
    minusDays(dayOfWeek.value - 1L)

fun LocalDate.endOfWeek(): LocalDate =
    plusDays(7L - dayOfWeek.value)

fun LocalDate.startOfMonth(): LocalDate =
    withDayOfMonth(1)

fun LocalDate.endOfMonth(): LocalDate =
    plusMonths(1).withDayOfMonth(1).minusDays(1)
```

## 조건부 실행 확장

### 체이닝 확장
```kotlin
// 조건부 적용
inline fun <T> T.applyIf(condition: Boolean, block: T.() -> T): T =
    if (condition) block() else this

inline fun <T> T.applyIfNotNull(value: Any?, block: T.() -> T): T =
    if (value != null) block() else this

inline fun <T> T.applyUnless(condition: Boolean, block: T.() -> T): T =
    if (!condition) block() else this

// 조건부 변환
inline fun <T, R> T.takeIf(predicate: T.() -> Boolean, transform: T.() -> R): R? =
    if (predicate()) transform() else null

inline fun <T, R> T.takeUnless(predicate: T.() -> Boolean, transform: T.() -> R): R? =
    if (!predicate()) transform() else null

// Null 체크와 함께 실행
inline fun <T, R> T?.ifNotNull(block: (T) -> R): R? =
    this?.let(block)

inline fun <T> T?.ifNull(block: () -> T): T =
    this ?: block()
```

### 에러 처리 확장
```kotlin
// 안전한 실행
inline fun <T, R> T.runCatching(block: T.() -> R): Result<R> =
    try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    }

inline fun <T> runCatchingUnit(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    }

// 예외를 null로 변환
inline fun <T> T?.orThrow(exception: () -> Exception): T =
    this ?: throw exception()

inline fun <T> T?.orThrow(message: String): T =
    this ?: throw IllegalArgumentException(message)
```

## 비동기 확장

### Coroutines 확장
```kotlin
// 재시도 로직
suspend inline fun <T> retryWithBackoff(
    maxRetries: Int = 3,
    initialDelayMs: Long = 1000,
    factor: Double = 2.0,
    crossinline block: suspend () -> T
): T {
    var currentDelay = initialDelayMs
    repeat(maxRetries - 1) {
        try {
            return block()
        } catch (e: Exception) {
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong()
        }
    }
    return block() // 마지막 시도
}

// 타임아웃과 함께 실행
suspend inline fun <T> withTimeoutResult(
    timeoutMillis: Long,
    crossinline block: suspend () -> T
): Result<T> = try {
    Result.success(withTimeout(timeoutMillis) { block() })
} catch (e: TimeoutCancellationException) {
    Result.failure(e)
} catch (e: Exception) {
    Result.failure(e)
}

// 병렬 처리
suspend fun <T, R> Collection<T>.mapParallel(
    transform: suspend (T) -> R
): List<R> = coroutineScope {
    map { async { transform(it) } }.awaitAll()
}

suspend fun <T, R> Collection<T>.mapParallelNotNull(
    transform: suspend (T) -> R?
): List<R> = coroutineScope {
    map { async { transform(it) } }
        .awaitAll()
        .filterNotNull()
}
```

## 검증 확장

### 데이터 검증 DSL
```kotlin
class ValidationResult private constructor(
    private val errors: List<String>
) {
    val isValid: Boolean get() = errors.isEmpty()
    val isInvalid: Boolean get() = errors.isNotEmpty()
    val errorMessages: List<String> get() = errors.toList()
    
    companion object {
        fun valid(): ValidationResult = ValidationResult(emptyList())
        fun invalid(vararg messages: String): ValidationResult = 
            ValidationResult(messages.toList())
    }
    
    operator fun plus(other: ValidationResult): ValidationResult =
        ValidationResult(errors + other.errors)
}

inline fun <T> T.validate(block: ValidationBuilder<T>.() -> Unit): ValidationResult {
    val builder = ValidationBuilder(this)
    builder.block()
    return builder.build()
}

class ValidationBuilder<T>(private val value: T) {
    private val errors = mutableListOf<String>()
    
    fun require(condition: Boolean, message: String) {
        if (!condition) {
            errors.add(message)
        }
    }
    
    fun require(condition: T.() -> Boolean, message: String) {
        if (!value.condition()) {
            errors.add(message)
        }
    }
    
    fun String?.notBlank(fieldName: String) {
        if (this.isNullOrBlank()) {
            errors.add("$fieldName 은(는) 필수입니다")
        }
    }
    
    fun String?.email(fieldName: String) {
        if (this != null && !this.isValidEmail()) {
            errors.add("$fieldName 은(는) 유효한 이메일 형식이어야 합니다")
        }
    }
    
    fun Number?.positive(fieldName: String) {
        if (this != null && this.toDouble() <= 0) {
            errors.add("$fieldName 은(는) 양수여야 합니다")
        }
    }
    
    fun <E> Collection<E>?.notEmpty(fieldName: String) {
        if (this.isNullOrEmpty()) {
            errors.add("$fieldName 은(는) 비어있을 수 없습니다")
        }
    }
    
    internal fun build(): ValidationResult =
        if (errors.isEmpty()) ValidationResult.valid()
        else ValidationResult.invalid(*errors.toTypedArray())
}

// 사용 예시
data class CreateUserRequest(
    val email: String?,
    val name: String?,
    val age: Int?
) {
    fun validate(): ValidationResult = validate {
        email.notBlank("이메일")
        email.email("이메일")
        name.notBlank("이름")
        age.positive("나이")
        require(age != null && age < 120) { "나이는 120세 미만이어야 합니다" }
    }
}
```

## 함수형 프로그래밍 확장

### 모나드 스타일 확장
```kotlin
// Maybe 모나드
sealed class Maybe<out T> {
    object None : Maybe<Nothing>()
    data class Some<T>(val value: T) : Maybe<T>()
    
    companion object {
        fun <T> of(value: T?): Maybe<T> =
            if (value != null) Some(value) else None
        
        fun <T> none(): Maybe<T> = None
        fun <T> some(value: T): Maybe<T> = Some(value)
    }
    
    inline fun <R> map(transform: (T) -> R): Maybe<R> = when (this) {
        is None -> None
        is Some -> Some(transform(value))
    }
    
    inline fun <R> flatMap(transform: (T) -> Maybe<R>): Maybe<R> = when (this) {
        is None -> None
        is Some -> transform(value)
    }
    
    inline fun filter(predicate: (T) -> Boolean): Maybe<T> = when (this) {
        is None -> None
        is Some -> if (predicate(value)) this else None
    }
    
    fun orElse(default: T): T = when (this) {
        is None -> default
        is Some -> value
    }
    
    inline fun orElseGet(supplier: () -> T): T = when (this) {
        is None -> supplier()
        is Some -> value
    }
}

fun <T> T?.toMaybe(): Maybe<T> = Maybe.of(this)
```

### 함수 조합 확장
```kotlin
// 함수 합성
infix fun <A, B, C> ((A) -> B).andThen(f: (B) -> C): (A) -> C = { a -> f(this(a)) }
infix fun <A, B, C> ((B) -> C).compose(f: (A) -> B): (A) -> C = { a -> this(f(a)) }

// 커링
fun <A, B, C> ((A, B) -> C).curry(): (A) -> (B) -> C = { a -> { b -> this(a, b) } }
fun <A, B, C> ((A) -> (B) -> C).uncurry(): (A, B) -> C = { a, b -> this(a)(b) }

// 메모이제이션
fun <T, R> ((T) -> R).memoize(): (T) -> R {
    val cache = mutableMapOf<T, R>()
    return { input ->
        cache.getOrPut(input) { this(input) }
    }
}

// 부분 적용
fun <A, B, C> ((A, B) -> C).partial1(a: A): (B) -> C = { b -> this(a, b) }
fun <A, B, C> ((A, B) -> C).partial2(b: B): (A) -> C = { a -> this(a, b) }
```

## 사용 예시

### 실제 도메인에서의 활용
```kotlin
// User 도메인에서의 활용
class UserService {
    
    fun createUser(request: CreateUserRequest): Result<User> {
        // 검증
        val validationResult = request.validate()
        if (validationResult.isInvalid) {
            return Result.failure(ValidationException(validationResult.errorMessages))
        }
        
        // 데이터 변환 및 처리
        val user = User(
            email = Email.of(request.email!!),
            name = request.name!!.normalizeWhitespace(),
            phone = request.phone?.normalizePhoneNumber(),
            age = request.age!!
        )
        
        return runCatching {
            userRepository.save(user)
        }
    }
    
    suspend fun processUsers(userIds: List<Long>): List<ProcessResult> {
        return userIds
            .chunkedBy(10) // 10개씩 배치 처리
            .mapParallel { batch ->
                batch.mapParallelNotNull { userId ->
                    retryWithBackoff(maxRetries = 3) {
                        processUser(userId)
                    }
                }
            }
            .flatten()
    }
}
```

이러한 확장 함수들을 통해 Kotlin 코드를 더욱 간결하고 표현력 있게 작성할 수 있으며, 반복되는 패턴들을 재사용 가능한 유틸리티로 추상화할 수 있습니다.