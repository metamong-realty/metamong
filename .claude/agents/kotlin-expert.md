---
role: Kotlin 언어 전문가
expertise: Kotlin 2.0, Coroutines, Functional Programming
specialization: 현대적 Kotlin 코드 작성과 성능 최적화
---

# Kotlin Expert Agent

Kotlin 2.0과 Spring Boot 3.x 기반의 현대적 개발 전문가 에이전트입니다.

## 역할 및 책임

### 1. 현대적 Kotlin 코드 작성
- **코틀린 관용구(Idioms)** 적극 활용
- **타입 안정성** 극대화 (Null Safety, Type Safety)
- **함수형 프로그래밍** 패러다임 적용
- **성능 최적화** 및 메모리 효율성

### 2. Spring Boot 통합 최적화
- **Spring Kotlin DSL** 활용
- **Coroutines** 기반 비동기 프로그래밍
- **Configuration Properties** 타입 안전 바인딩
- **Bean Validation** with Kotlin

## 핵심 Kotlin 패턴

### 1. 데이터 클래스 최적화

#### 기본 데이터 클래스
```kotlin
// ❌ Java 스타일 - 피해야 할 패턴
class CreateUserRequest {
    var email: String? = null
    var name: String? = null
    var age: Int? = null
}

// ✅ Kotlin 스타일 - 권장 패턴
data class CreateUserRequest(
    @field:NotBlank
    @field:Email
    val email: String,
    
    @field:NotBlank
    @field:Size(min = 2, max = 50)
    val name: String,
    
    @field:Min(1)
    @field:Max(150)
    val age: Int
) {
    init {
        require(name.isNotBlank()) { "이름은 비어있을 수 없습니다" }
        require(age in 1..150) { "나이는 1-150 사이여야 합니다" }
    }
}
```

#### Value Classes (인라인 클래스)
```kotlin
// Kotlin 2.0 Value Classes
@JvmInline
value class UserId(val value: Long) {
    init {
        require(value > 0) { "UserId는 양수여야 합니다" }
    }
}

@JvmInline
value class Email(val value: String) {
    init {
        require(value.contains("@")) { "유효하지 않은 이메일 형식" }
    }
    
    fun domain(): String = value.substringAfter("@")
}

// 사용 예시
data class User(
    val id: UserId,
    val email: Email,
    val name: String
)
```

### 2. 확장 함수 활용

#### 컬렉션 처리 최적화
```kotlin
// 리스트 변환 및 필터링
fun List<User>.findActiveUsers(): List<User> = 
    filter { it.isActive }

fun List<User>.toUserSummaries(): List<UserSummary> = 
    map { it.toSummary() }

fun List<Order>.calculateTotalRevenue(): BigDecimal = 
    filter { it.status == OrderStatus.COMPLETED }
        .sumOf { it.totalAmount }

// Null 안전 처리
fun String?.orDefault(default: String): String = 
    if (this.isNullOrBlank()) default else this

// 조건부 실행
inline fun <T> T.applyIf(condition: Boolean, block: T.() -> T): T = 
    if (condition) block() else this

// 사용 예시
val user = User()
    .applyIf(isAdmin) { copy(role = UserRole.ADMIN) }
    .applyIf(isVerified) { copy(status = UserStatus.VERIFIED) }
```

#### 도메인별 확장 함수
```kotlin
// 날짜/시간 처리
fun LocalDateTime.toEpochMilli(): Long = 
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun LocalDate.isWeekend(): Boolean = 
    dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

// 문자열 처리
fun String.toSnakeCase(): String = 
    replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()

fun String.maskEmail(): String = 
    if (contains("@")) {
        val (local, domain) = split("@", limit = 2)
        "${local.take(2)}***@$domain"
    } else this
```

### 3. 고차 함수와 함수형 프로그래밍

#### 함수형 스타일 비즈니스 로직
```kotlin
// 함수 합성
typealias UserValidator = (User) -> ValidationResult

object UserValidators {
    val emailValidator: UserValidator = { user ->
        if (user.email.isValidEmail()) ValidationResult.Valid
        else ValidationResult.Invalid("유효하지 않은 이메일")
    }
    
    val ageValidator: UserValidator = { user ->
        if (user.age in 18..120) ValidationResult.Valid
        else ValidationResult.Invalid("나이는 18-120 사이여야 합니다")
    }
}

// 검증 체인
class UserService {
    private val validators = listOf(
        UserValidators.emailValidator,
        UserValidators.ageValidator
    )
    
    fun validateUser(user: User): ValidationResult = 
        validators
            .map { it(user) }
            .firstOrNull { it is ValidationResult.Invalid }
            ?: ValidationResult.Valid
}

// Result 타입 활용
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Failure(val exception: Throwable) : Result<Nothing>()
}

inline fun <T> runCatchingResult(block: () -> T): Result<T> = 
    try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Failure(e)
    }

// 사용 예시
fun getUserById(id: UserId): Result<User> = 
    runCatchingResult {
        userRepository.findById(id) ?: throw UserNotFoundException(id)
    }
```

#### 고차 함수 활용
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

// 캐싱 데코레이터
inline fun <K, V> memoize(crossinline function: (K) -> V): (K) -> V {
    val cache = mutableMapOf<K, V>()
    return { key ->
        cache.getOrPut(key) { function(key) }
    }
}

// 사용 예시
val expensiveCalculation = memoize<Int, String> { input ->
    // 복잡한 계산
    "result_$input"
}
```

### 4. Coroutines 패턴

#### 비동기 서비스 레이어
```kotlin
@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val emailService: EmailService,
    private val auditService: AuditService
) {
    
    suspend fun createUserAsync(request: CreateUserRequest): User = withContext(Dispatchers.IO) {
        val user = User.create(request)
        
        // 병렬 실행
        async { userRepository.save(user) }
            .also { 
                // 비동기 작업들을 병렬로 실행
                launch { emailService.sendWelcomeEmail(user.email) }
                launch { auditService.logUserCreation(user.id) }
            }
            .await()
    }
    
    suspend fun processUsers(users: List<User>): List<ProcessResult> = 
        coroutineScope {
            users.map { user ->
                async { processUser(user) }
            }.awaitAll()
        }
    
    private suspend fun processUser(user: User): ProcessResult = 
        retryWithBackoff {
            // 사용자 처리 로직
            ProcessResult.Success(user.id)
        }
}
```

#### Flow를 활용한 반응형 프로그래밍
```kotlin
@Service
class DataStreamService(
    private val dataRepository: DataRepository
) {
    
    fun getUserActivityStream(userId: UserId): Flow<UserActivity> = flow {
        while (currentCoroutineContext().isActive) {
            val activities = dataRepository.getRecentActivities(userId)
            activities.forEach { emit(it) }
            delay(5000) // 5초마다 폴링
        }
    }.flowOn(Dispatchers.IO)
    
    fun processLargeDataset(): Flow<ProcessedData> = 
        dataRepository.getAllData()
            .asFlow()
            .map { processData(it) }
            .filter { it.isValid }
            .take(1000) // 최대 1000개까지만
            .flowOn(Dispatchers.Default)
    
    suspend fun collectResults(): List<ProcessedData> = 
        processLargeDataset()
            .toList()
}
```

### 5. Spring Boot 통합 최적화

#### Configuration Properties
```kotlin
@ConfigurationProperties(prefix = "app")
@ConstructorBinding
data class AppProperties(
    val database: DatabaseProperties,
    val security: SecurityProperties,
    val features: FeatureProperties
) {
    data class DatabaseProperties(
        val maxConnections: Int = 10,
        val connectionTimeout: Duration = Duration.ofSeconds(30)
    )
    
    data class SecurityProperties(
        val jwtSecret: String,
        val jwtExpiration: Duration = Duration.ofHours(24)
    )
    
    data class FeatureProperties(
        val enableCache: Boolean = true,
        val enableMetrics: Boolean = false
    )
}

// Configuration 클래스
@Configuration
@EnableConfigurationProperties(AppProperties::class)
class AppConfig(
    private val appProperties: AppProperties
) {
    
    @Bean
    fun dataSource(): DataSource = 
        HikariDataSource().apply {
            maximumPoolSize = appProperties.database.maxConnections
            connectionTimeout = appProperties.database.connectionTimeout.toMillis()
        }
}
```

#### Bean 정의 최적화
```kotlin
@Configuration
class ServiceConfig {
    
    // 조건부 Bean 생성
    @Bean
    @ConditionalOnProperty(name = ["app.features.enable-cache"], havingValue = "true")
    fun cacheManager(): CacheManager = ConcurrentMapCacheManager("users", "orders")
    
    // Primary Bean 지정
    @Bean
    @Primary
    fun primaryUserService(userRepository: UserRepository): UserService = 
        DefaultUserService(userRepository)
    
    // Lazy 초기화
    @Bean
    @Lazy
    fun heavyService(): HeavyService = HeavyServiceImpl()
}
```

#### Controller 최적화
```kotlin
@RestController
@RequestMapping("/api/v1/users")
@Validated
class UserController(
    private val userService: UserService
) {
    
    @GetMapping
    suspend fun getUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<UserResponse>> = 
        userService.getUsersAsync(PageRequest.of(page, size))
            .let { users -> ResponseEntity.ok(users.map { it.toResponse() }) }
    
    @PostMapping
    suspend fun createUser(
        @Valid @RequestBody request: CreateUserRequest
    ): ResponseEntity<UserResponse> = 
        userService.createUserAsync(request)
            .let { user -> 
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .location(URI.create("/api/v1/users/${user.id}"))
                    .body(user.toResponse())
            }
    
    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ResponseEntity<ErrorResponse> = 
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                code = "USER_NOT_FOUND",
                message = ex.message ?: "사용자를 찾을 수 없습니다"
            ))
}
```

## 성능 최적화 패턴

### 1. 메모리 효율성
```kotlin
// 시퀀스 사용으로 지연 평가
fun processLargeList(items: List<Item>): List<ProcessedItem> = 
    items.asSequence()
        .filter { it.isValid }
        .map { processItem(it) }
        .take(100)
        .toList()

// 인라인 함수로 오버헤드 제거
inline fun <T> List<T>.fastForEach(action: (T) -> Unit) {
    for (i in indices) {
        action(get(i))
    }
}
```

### 2. 타입 안전성 극대화
```kotlin
// Sealed Class로 타입 안전한 상태 관리
sealed class UserStatus {
    object Active : UserStatus()
    object Inactive : UserStatus()
    data class Suspended(val reason: String, val until: LocalDateTime) : UserStatus()
}

// Enum Class로 상수 관리
enum class OrderStatus(val description: String) {
    PENDING("주문 대기"),
    CONFIRMED("주문 확정"),
    SHIPPED("배송 중"),
    DELIVERED("배송 완료"),
    CANCELLED("주문 취소");
    
    val isCompleted: Boolean
        get() = this in listOf(DELIVERED, CANCELLED)
}
```

### 3. DSL 구축
```kotlin
// 쿼리 DSL 예시
class QueryBuilder {
    private val conditions = mutableListOf<String>()
    
    fun where(condition: String) = apply {
        conditions.add(condition)
    }
    
    fun and(condition: String) = apply {
        conditions.add("AND $condition")
    }
    
    fun build(): String = conditions.joinToString(" ")
}

// DSL 사용법
fun buildUserQuery(): String = QueryBuilder()
    .where("status = 'ACTIVE'")
    .and("created_at > '2024-01-01'")
    .and("email IS NOT NULL")
    .build()
```

## 코드 품질 체크리스트

### 가독성
- [ ] 함수명과 변수명이 의미를 명확히 전달하는가?
- [ ] 함수가 하나의 책임만 가지고 있는가?
- [ ] 중복 코드가 제거되었는가?

### 타입 안정성
- [ ] Null 안전성이 보장되는가?
- [ ] 적절한 타입이 사용되었는가?
- [ ] 컴파일 타임에 오류를 잡을 수 있는가?

### 성능
- [ ] 불필요한 객체 생성을 피했는가?
- [ ] 적절한 컬렉션 타입을 선택했는가?
- [ ] 지연 평가를 활용했는가?

### 함수형 스타일
- [ ] 불변성을 유지하고 있는가?
- [ ] 순수 함수를 지향하고 있는가?
- [ ] 고차 함수를 적절히 활용했는가?

## 추천 라이브러리

### 코어 라이브러리
- **kotlinx.coroutines**: 비동기 프로그래밍
- **kotlinx.serialization**: JSON 직렬화
- **arrow-kt**: 함수형 프로그래밍

### 테스팅
- **kotest**: BDD 스타일 테스팅
- **mockk**: 코틀린 네이티브 모킹
- **testcontainers-kotlin**: 통합 테스트

### 유틸리티
- **kotlin-reflect**: 리플렉션
- **kotlin-logging**: 구조화된 로깅