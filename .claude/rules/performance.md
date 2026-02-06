# Performance Guidelines

## 성능 최적화 원칙

### Big O 복잡도 고려
```kotlin
// Good - O(1) HashMap 조회
val userMap = users.associateBy { it.id }
val user = userMap[userId]

// Bad - O(n) 리스트 검색
val user = users.find { it.id == userId }

// Good - O(log n) 이진 검색 (정렬된 리스트)
val sortedUsers = users.sortedBy { it.id }
val index = sortedUsers.binarySearch { it.id.compareTo(userId) }

// Good - O(n) 단일 순회
val result = users.mapNotNull { user ->
    if (user.isActive) user.toDto() else null
}

// Bad - O(n²) 중첩 반복문
users.forEach { user ->
    posts.forEach { post ->  // 가능한 피해야 함
        if (post.userId == user.id) {
            // 처리
        }
    }
}
```

### 메모리 효율성
```kotlin
// Good - sequence로 지연 계산
fun processLargeData(data: List<Item>): List<ProcessedItem> {
    return data.asSequence()
        .filter { it.isValid() }
        .map { processItem(it) }
        .take(100)
        .toList()
}

// Bad - 중간 컬렉션 생성
fun processLargeData(data: List<Item>): List<ProcessedItem> {
    return data
        .filter { it.isValid() }  // 새 리스트 생성
        .map { processItem(it) }  // 또 다른 새 리스트 생성
        .take(100)
        .toList()
}
```

## 데이터베이스 성능

### N+1 문제 해결
```kotlin
// Bad - N+1 문제 발생
fun getBoardsWithAuthors(): List<BoardDto> {
    val boards = boardRepository.findAll()
    return boards.map { board ->
        val author = userRepository.findById(board.authorId)  // N번 쿼리
        BoardDto(board, author)
    }
}

// Good - Batch 조회
fun getBoardsWithAuthors(): List<BoardDto> {
    val boards = boardRepository.findAll()
    val authorIds = boards.map { it.authorId }.distinct()
    val authors = userRepository.findAllById(authorIds)
        .associateBy { it.id }
    
    return boards.map { board ->
        val author = authors[board.authorId]!!
        BoardDto(board, author)
    }
}

// Best - QueryDSL Join
fun getBoardsWithAuthors(): List<BoardWithAuthorProjection> {
    return queryFactory
        .select(
            Projections.constructor(
                BoardWithAuthorProjection::class.java,
                board.id,
                board.title,
                board.content,
                user.nickname
            )
        )
        .from(board)
        .innerJoin(user).on(board.authorId.eq(user.id))
        .fetch()
}
```

### 인덱스 최적화
```kotlin
@Entity
@Table(
    indexes = [
        Index(name = "idx_user_email", columnList = "email"),
        Index(name = "idx_user_status_created", columnList = "status,created_at"),
        Index(name = "idx_post_author_created", columnList = "author_id,created_at")
    ]
)
class User
```

### 페이징 성능
```kotlin
// Good - 오프셋 없는 페이징
fun getNextPosts(lastId: Long?, size: Int): List<Post> {
    val query = queryFactory.selectFrom(post)
    
    lastId?.let {
        query.where(post.id.lt(it))
    }
    
    return query
        .orderBy(post.id.desc())
        .limit(size.toLong())
        .fetch()
}

// Bad - OFFSET 사용 (큰 오프셋에서 느림)
fun getPostsWithOffset(page: Int, size: Int): Page<Post> {
    val pageable = PageRequest.of(page, size)
    return postRepository.findAll(pageable)  // OFFSET 사용
}
```

## 캐싱 전략

### Spring Cache 활용
```kotlin
@Service
class UserService(
    private val userRepository: UserRepository
) {
    
    @Cacheable(value = ["users"], key = "#id")
    fun getUser(id: Long): User? {
        return userRepository.findByIdOrNull(id)
    }
    
    @CacheEvict(value = ["users"], key = "#user.id")
    fun updateUser(user: User): User {
        return userRepository.save(user)
    }
    
    @Cacheable(
        value = ["user-statistics"], 
        key = "#userId",
        condition = "#result != null",
        unless = "#result.postCount == 0"
    )
    fun getUserStatistics(userId: Long): UserStatistics? {
        return userRepository.findUserStatistics(userId)
    }
}
```

### Redis 캐싱 패턴
```kotlin
@Service
class PostService(
    private val postRepository: PostRepository,
    private val redisService: RedisService
) {
    
    // Cache-Aside 패턴
    fun getPopularPosts(): List<Post> {
        val cacheKey = "popular_posts"
        
        return redisService.getValue(cacheKey, List::class.java) as? List<Post>
            ?: run {
                val posts = postRepository.findPopularPosts()
                redisService.setValue(cacheKey, posts, Duration.ofMinutes(10))
                posts
            }
    }
    
    // Write-Through 패턴
    fun updatePost(post: Post): Post {
        val updated = postRepository.save(post)
        redisService.setValue("post:${post.id}", updated, Duration.ofHours(1))
        return updated
    }
}
```

### 분산 락으로 캐시 갱신 최적화
```kotlin
@Service
class CacheService(
    private val redisLockService: RedisLockService,
    private val redisService: RedisService
) {
    
    fun getOrSetCache(key: String, supplier: () -> Any): Any? {
        val cached = redisService.getValue(key, Any::class.java)
        if (cached != null) return cached
        
        // 동시 캐시 갱신 방지
        return redisLockService.executeWithLock("cache_lock:$key") {
            // 락 획득 후 다시 확인
            redisService.getValue(key, Any::class.java) ?: run {
                val value = supplier()
                redisService.setValue(key, value, Duration.ofMinutes(10))
                value
            }
        }
    }
}
```

## HTTP 성능 최적화

### 응답 압축
```yaml
server:
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain
    min-response-size: 1024
```

### ETags 활용
```kotlin
@GetMapping("/{id}")
fun getUser(
    @PathVariable id: Long,
    request: WebRequest
): ResponseEntity<UserResponse> {
    val user = userService.getUser(id)
        ?: return ResponseEntity.notFound().build()
    
    val etag = "\"${user.updatedAt.toEpochSecond()}\""
    
    // 클라이언트 캐시 검증
    if (request.checkNotModified(etag)) {
        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build()
    }
    
    return ResponseEntity.ok()
        .eTag(etag)
        .body(UserResponse.from(user))
}
```

## 비동기 처리

### 코루틴 활용
```kotlin
@Service
class UserService {
    
    suspend fun getUserWithDetails(userId: Long): UserWithDetails {
        return coroutineScope {
            val user = async { userRepository.findById(userId) }
            val posts = async { postRepository.findByUserId(userId) }
            val followers = async { followRepository.findFollowersByUserId(userId) }
            
            UserWithDetails(
                user = user.await(),
                posts = posts.await(),
                followers = followers.await()
            )
        }
    }
}
```

### 이벤트 기반 비동기
```kotlin
@Service
class OrderService(
    private val eventPublisher: ApplicationEventPublisher
) {
    
    @Transactional
    fun createOrder(request: CreateOrderRequest): Order {
        val order = Order.from(request)
        val savedOrder = orderRepository.save(order)
        
        // 비동기 이벤트 발생
        eventPublisher.publishEvent(OrderCreatedEvent(savedOrder.id))
        
        return savedOrder
    }
}

@EventListener
@Async
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
fun handleOrderCreated(event: OrderCreatedEvent) {
    // 이메일 발송, 재고 업데이트 등
}
```

## 프로파일링 및 모니터링

### JVM 메트릭
```kotlin
@Configuration
class MetricsConfig {
    
    @Bean
    fun meterRegistryCustomizer(): MeterRegistryCustomizer<MeterRegistry> {
        return MeterRegistryCustomizer { registry ->
            registry.config()
                .commonTags("application", "metamong")
        }
    }
}

@Service
class UserService(
    private val meterRegistry: MeterRegistry
) {
    private val userCreationCounter = Counter.builder("user.created")
        .description("Number of users created")
        .register(meterRegistry)
    
    fun createUser(request: CreateUserRequest): User {
        val user = userRepository.save(User.from(request))
        userCreationCounter.increment()
        return user
    }
}
```

### 성능 측정
```kotlin
@Component
class PerformanceAspect {
    
    @Around("@annotation(Performance)")
    fun measurePerformance(joinPoint: ProceedingJoinPoint): Any? {
        val startTime = System.currentTimeMillis()
        
        return try {
            joinPoint.proceed()
        } finally {
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            
            logger.info { 
                "Method: ${joinPoint.signature.name}, Duration: ${duration}ms" 
            }
            
            if (duration > 1000) {
                logger.warn { "Slow method detected: ${joinPoint.signature.name}" }
            }
        }
    }
}
```

## 메모리 관리

### 메모리 누수 방지
```kotlin
// Good - 스트림 자동 종료
fun readFile(path: String): String {
    return File(path).bufferedReader().use { reader ->
        reader.readText()
    }
}

// Bad - 리소스 누수 가능성
fun readFile(path: String): String {
    val reader = File(path).bufferedReader()
    return reader.readText()  // reader가 닫히지 않음
}

// Good - WeakHashMap으로 메모리 누수 방지
private val cache = WeakHashMap<String, ExpensiveObject>()

// Good - 타이머 정리
class ScheduledService {
    private val timer = Timer()
    
    @PreDestroy
    fun cleanup() {
        timer.cancel()
    }
}
```

### OutOfMemoryError 방지
```kotlin
// Good - 스트리밍 처리
fun processLargeFile(file: File): Result {
    return file.bufferedReader().useLines { lines ->
        lines
            .filter { it.isNotBlank() }
            .map { processLine(it) }
            .reduce { acc, result -> acc.merge(result) }
    }
}

// Bad - 전체 파일 메모리 로드
fun processLargeFile(file: File): Result {
    val allLines = file.readLines()  // 전체 파일을 메모리에 로드
    return allLines.map { processLine(it) }.reduce { acc, result -> acc.merge(result) }
}
```

## Connection Pool 튜닝

### HikariCP 설정
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20        # CPU 코어 수 * 2
      minimum-idle: 10             # maximum의 50%
      connection-timeout: 20000    # 20초
      idle-timeout: 300000         # 5분
      max-lifetime: 1200000        # 20분
      leak-detection-threshold: 60000  # 1분
```

## 성능 테스트

### JMH 마이크로 벤치마크
```kotlin
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
open class StringConcatBenchmark {
    
    @Param("10", "100", "1000")
    var size: Int = 0
    
    @Benchmark
    fun stringBuilder(): String {
        val sb = StringBuilder()
        repeat(size) { sb.append("test") }
        return sb.toString()
    }
    
    @Benchmark
    fun stringPlus(): String {
        var result = ""
        repeat(size) { result += "test" }
        return result
    }
}
```

### 부하 테스트
```kotlin
@SpringBootTest
class LoadTest {
    
    @Test
    fun `100명 동시 사용자 로그인 테스트`() {
        val executor = Executors.newFixedThreadPool(100)
        val latch = CountDownLatch(100)
        val results = Collections.synchronizedList(mutableListOf<Boolean>())
        
        repeat(100) {
            executor.submit {
                try {
                    val success = authService.login("user$it@test.com", "password")
                    results.add(success)
                } finally {
                    latch.countDown()
                }
            }
        }
        
        latch.await(30, TimeUnit.SECONDS)
        
        val successCount = results.count { it }
        successCount shouldBeGreaterThan 95  // 95% 이상 성공률
    }
}
```

## 성능 체크리스트

### 코드 레벨
- [ ] Big O 복잡도 고려
- [ ] 불필요한 객체 생성 최소화
- [ ] Stream vs Collection 적절한 선택
- [ ] 리소스 자동 해제 (use, try-with-resources)
- [ ] 메모리 누수 방지

### 데이터베이스
- [ ] N+1 문제 해결
- [ ] 적절한 인덱스 설정
- [ ] 페이징 최적화
- [ ] 쿼리 성능 분석
- [ ] Connection Pool 설정

### 캐싱
- [ ] 적절한 캐시 전략 선택
- [ ] TTL 설정
- [ ] 캐시 무효화 전략
- [ ] 분산 캐시 고려

### HTTP/네트워크
- [ ] 응답 압축 설정
- [ ] ETags 활용
- [ ] Keep-Alive 설정
- [ ] 적절한 타임아웃 설정