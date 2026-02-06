# Architecture Guidelines

## DDD (Domain-Driven Design) 원칙

### Entity 규칙
- 비즈니스 로직을 포함하는 도메인 객체
- ID로 식별 가능
- 생명주기 존재
- 상태 변경 메서드 제공
- Setter 사용 금지

```kotlin
@Entity
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var email: String,
    var nickname: String,
    private var password: String
) {
    fun changePassword(newPassword: String) {
        validatePassword(newPassword)
        this.password = newPassword
    }
    
    private fun validatePassword(password: String) {
        require(password.length >= 8) { "비밀번호는 8자 이상이어야 합니다" }
    }
}
```

### Value Object 규칙
- 불변 객체 (모든 필드 val)
- equals/hashCode 구현 (data class 활용)
- 자체 검증 로직 포함
- Entity의 속성으로 사용

```kotlin
@Embeddable
data class Email(
    val value: String
) {
    init {
        require(value.contains("@")) { "올바른 이메일 형식이 아닙니다" }
    }
}
```

### Aggregate 규칙
- 일관성 경계 정의
- Root Entity를 통해서만 접근
- 트랜잭션 경계와 일치
- 다른 Aggregate는 ID로만 참조

### Repository 규칙
- 도메인 레이어에 인터페이스 정의
- 인프라 레이어에 구현
- Aggregate 단위로 생성
- Collection처럼 동작

```kotlin
// Domain Layer
interface UserRepository {
    fun save(user: User): User
    fun findById(id: Long): User?
    fun findByEmail(email: String): User?
}

// Infrastructure Layer
@Repository
class UserRepositoryImpl(
    private val jpaRepository: UserJpaRepository
) : UserRepository {
    override fun save(user: User) = jpaRepository.save(user)
    override fun findById(id: Long) = jpaRepository.findByIdOrNull(id)
    override fun findByEmail(email: String) = jpaRepository.findByEmail(email)
}
```

## Clean Architecture 레이어 규칙

### 의존성 방향
```
Domain (중심)
    ↑
Application
    ↑
Infrastructure / Presentation
```

### Domain Layer
- 비즈니스 로직 중심
- 프레임워크 독립적
- 외부 의존성 없음
- 순수 Kotlin 코드

### Application Layer
- Use Case 구현
- 도메인 로직 조율
- 트랜잭션 관리
- 도메인 이벤트 처리

```kotlin
@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun createUser(command: CreateUserCommand): UserDto {
        val user = User(
            email = command.email,
            nickname = command.nickname,
            password = passwordEncoder.encode(command.password)
        )
        return UserDto.from(userRepository.save(user))
    }
}
```

### Infrastructure Layer
- 외부 시스템 연동
- 데이터베이스 구현
- 메시징 시스템
- 캐시 구현

### Presentation Layer
- REST API Controller
- Request/Response DTO
- 입력 검증
- API 문서화

```kotlin
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {
    @PostMapping
    fun createUser(
        @Valid @RequestBody request: CreateUserRequest
    ): ResponseEntity<UserResponse> {
        val command = request.toCommand()
        val user = userService.createUser(command)
        return ResponseEntity.ok(UserResponse.from(user))
    }
}
```

## SOLID 원칙 적용

### Single Responsibility
- 클래스는 하나의 책임만
- 메서드는 하나의 작업만
- 변경 이유는 하나만

### Open/Closed
- 확장에는 열려있고 수정에는 닫혀있음
- 인터페이스와 추상 클래스 활용
- Strategy 패턴 활용

### Liskov Substitution
- 하위 타입은 상위 타입을 대체 가능
- 계약 준수
- 예외를 좁히지 않기

### Interface Segregation
- 클라이언트별 인터페이스 분리
- 불필요한 의존성 제거
- 작은 인터페이스 선호

### Dependency Inversion
- 추상화에 의존
- 구체 클래스 의존 금지
- 의존성 주입 활용

## 패키지 구조

```
com.metamong/
├── domain/              # 도메인 레이어
│   ├── user/
│   │   ├── model/      # Entity, VO
│   │   ├── repository/ # Repository 인터페이스
│   │   └── service/    # Domain Service
│   └── post/
├── application/         # 애플리케이션 레이어
│   ├── user/
│   │   ├── service/    # Application Service
│   │   ├── dto/        # Service DTO
│   │   └── command/    # Command 객체
│   └── post/
├── infrastructure/      # 인프라 레이어
│   ├── persistence/    # JPA, MongoDB
│   ├── messaging/      # Kafka, RabbitMQ
│   └── external/       # 외부 API 연동
└── presentation/       # 프레젠테이션 레이어
    ├── api/           # REST Controller
    ├── dto/           # Request/Response
    └── docs/          # Swagger 설정
```

## 의존성 주입 규칙

### Constructor Injection
```kotlin
@Service
class UserService(
    private val userRepository: UserRepository,
    private val emailService: EmailService
) {
    // 생성자 주입으로 불변성 보장
}
```

### Interface 의존
```kotlin
// Good
class OrderService(
    private val paymentGateway: PaymentGateway  // 인터페이스
)

// Bad
class OrderService(
    private val iamportPayment: IamportPayment  // 구체 클래스
)
```

## 테스트 가능한 설계

### 모든 의존성은 주입 가능
### 비즈니스 로직과 인프라 분리
### Mock 가능한 인터페이스
### 순수 함수 선호

## 성능 고려사항

### Lazy Loading vs Eager Loading
- 기본은 Lazy
- N+1 문제 주의
- Fetch Join 활용

### 캐싱 전략
- Redis 활용
- @Cacheable 적절히 사용
- 캐시 무효화 전략

### 비동기 처리
- Coroutine 활용
- @Async 신중히 사용
- 이벤트 기반 아키텍처