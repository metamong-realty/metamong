---
skill: 도메인 패턴 라이브러리
category: Architecture
patterns: Entity, Repository, Service, ValueObject
---

# Domain Patterns Skill

도메인 주도 설계의 핵심 패턴들을 구현하기 위한 스킬 라이브러리입니다.

## Entity 패턴

### BaseEntity 추상 클래스
```kotlin
@MappedSuperclass
abstract class BaseEntity<T> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    abstract val id: T
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
    
    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
        protected set
    
    @Version
    var version: Long = 0
        protected set
    
    // 도메인 이벤트 관리
    @Transient
    private val domainEvents = mutableListOf<DomainEvent>()
    
    protected fun addDomainEvent(event: DomainEvent) {
        domainEvents.add(event)
    }
    
    fun clearDomainEvents() {
        domainEvents.clear()
    }
    
    fun getDomainEvents(): List<DomainEvent> = domainEvents.toList()
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as BaseEntity<*>
        return id == other.id
    }
    
    override fun hashCode(): Int = id?.hashCode() ?: 0
    
    override fun toString(): String = "${javaClass.simpleName}(id=$id)"
}
```

### AggregateRoot 패턴
```kotlin
@MappedSuperclass
abstract class AggregateRoot<T> : BaseEntity<T>() {
    
    // 비즈니스 불변조건 검증
    abstract fun validate()
    
    // Aggregate 내부 상태 변경 시 호출
    protected fun markModified() {
        updatedAt = LocalDateTime.now()
    }
    
    // 비즈니스 규칙 검증 헬퍼
    protected fun require(condition: Boolean, lazyMessage: () -> String) {
        if (!condition) {
            throw DomainException(lazyMessage())
        }
    }
    
    protected fun ensure(condition: Boolean, lazyMessage: () -> String) {
        if (!condition) {
            throw BusinessRuleViolationException(lazyMessage())
        }
    }
}
```

### 도메인 Entity 예시
```kotlin
@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override val id: Long = 0,
    
    @Embedded
    var email: Email,
    
    var name: String,
    
    @Enumerated(EnumType.STRING)
    var status: UserStatus = UserStatus.ACTIVE,
    
    @Embedded
    var profile: UserProfile = UserProfile()
) : AggregateRoot<Long>() {
    
    companion object {
        fun create(
            email: Email,
            name: String,
            profile: UserProfile = UserProfile()
        ): User {
            val user = User(
                email = email,
                name = name,
                profile = profile
            )
            
            user.addDomainEvent(UserCreatedEvent(user.id, email.value))
            user.validate()
            
            return user
        }
    }
    
    fun updateProfile(newProfile: UserProfile) {
        require(status == UserStatus.ACTIVE) {
            "비활성 사용자는 프로필을 변경할 수 없습니다"
        }
        
        val oldProfile = profile
        profile = newProfile
        markModified()
        
        addDomainEvent(UserProfileUpdatedEvent(id, oldProfile, newProfile))
    }
    
    fun activate() {
        ensure(status != UserStatus.DELETED) {
            "삭제된 사용자는 활성화할 수 없습니다"
        }
        
        if (status != UserStatus.ACTIVE) {
            status = UserStatus.ACTIVE
            markModified()
            addDomainEvent(UserActivatedEvent(id))
        }
    }
    
    fun deactivate() {
        if (status == UserStatus.ACTIVE) {
            status = UserStatus.INACTIVE
            markModified()
            addDomainEvent(UserDeactivatedEvent(id))
        }
    }
    
    override fun validate() {
        require(name.isNotBlank()) { "사용자 이름은 필수입니다" }
        require(email.isValid()) { "유효한 이메일이 필요합니다" }
    }
    
    fun isActive(): Boolean = status == UserStatus.ACTIVE
    fun isInactive(): Boolean = status == UserStatus.INACTIVE
    fun isDeleted(): Boolean = status == UserStatus.DELETED
}

enum class UserStatus {
    ACTIVE, INACTIVE, DELETED
}
```

## Value Object 패턴

### 기본 Value Object 인터페이스
```kotlin
interface ValueObject {
    fun validate()
}

abstract class SingleValueObject<T>(val value: T) : ValueObject {
    
    init {
        validate()
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as SingleValueObject<*>
        return value == other.value
    }
    
    override fun hashCode(): Int = value?.hashCode() ?: 0
    
    override fun toString(): String = "${javaClass.simpleName}(value=$value)"
}
```

### Email Value Object
```kotlin
@Embeddable
data class Email(
    @Column(name = "email", nullable = false)
    private val _value: String
) : SingleValueObject<String>(_value) {
    
    val value: String get() = _value
    
    companion object {
        private val EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        
        fun of(email: String): Email {
            return Email(email.trim().lowercase())
        }
    }
    
    override fun validate() {
        require(value.isNotBlank()) { "이메일은 비어있을 수 없습니다" }
        require(value.matches(EMAIL_PATTERN)) { "유효하지 않은 이메일 형식입니다: $value" }
        require(value.length <= 254) { "이메일이 너무 깁니다: ${value.length}자" }
    }
    
    fun isValid(): Boolean = try {
        validate()
        true
    } catch (e: Exception) {
        false
    }
    
    fun domain(): String = value.substringAfter("@")
    fun localPart(): String = value.substringBefore("@")
    fun masked(): String = "${localPart().take(2)}***@${domain()}"
}
```

### Money Value Object
```kotlin
@Embeddable
data class Money(
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private val _amount: BigDecimal,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private val _currency: Currency = Currency.KRW
) : ValueObject {
    
    val amount: BigDecimal get() = _amount
    val currency: Currency get() = _currency
    
    companion object {
        val ZERO = Money(BigDecimal.ZERO)
        
        fun of(amount: Number, currency: Currency = Currency.KRW): Money {
            return Money(BigDecimal.valueOf(amount.toDouble()), currency)
        }
        
        fun won(amount: Number): Money = Money(BigDecimal.valueOf(amount.toDouble()), Currency.KRW)
        fun usd(amount: Number): Money = Money(BigDecimal.valueOf(amount.toDouble()), Currency.USD)
    }
    
    override fun validate() {
        require(amount >= BigDecimal.ZERO) { "금액은 0 이상이어야 합니다: $amount" }
        require(amount.scale() <= 2) { "금액은 소수점 둘째 자리까지만 허용됩니다" }
    }
    
    operator fun plus(other: Money): Money {
        ensureSameCurrency(other)
        return Money(amount + other.amount, currency)
    }
    
    operator fun minus(other: Money): Money {
        ensureSameCurrency(other)
        val result = amount - other.amount
        require(result >= BigDecimal.ZERO) { "결과가 음수가 될 수 없습니다: $result" }
        return Money(result, currency)
    }
    
    operator fun times(multiplier: Number): Money {
        return Money(amount * BigDecimal.valueOf(multiplier.toDouble()), currency)
    }
    
    fun isZero(): Boolean = amount == BigDecimal.ZERO
    fun isPositive(): Boolean = amount > BigDecimal.ZERO
    fun isGreaterThan(other: Money): Boolean {
        ensureSameCurrency(other)
        return amount > other.amount
    }
    
    private fun ensureSameCurrency(other: Money) {
        require(currency == other.currency) {
            "통화가 다릅니다: $currency vs ${other.currency}"
        }
    }
    
    fun formatted(): String = when (currency) {
        Currency.KRW -> "${amount.toPlainString()}원"
        Currency.USD -> "$${amount.toPlainString()}"
        Currency.EUR -> "€${amount.toPlainString()}"
    }
}

enum class Currency {
    KRW, USD, EUR
}
```

## Repository 패턴

### 기본 Repository 인터페이스
```kotlin
interface Repository<T, ID> {
    fun save(entity: T): T
    fun saveAll(entities: List<T>): List<T>
    fun findById(id: ID): T?
    fun existsById(id: ID): Boolean
    fun findAll(): List<T>
    fun deleteById(id: ID)
    fun delete(entity: T)
    fun count(): Long
}

interface ReadOnlyRepository<T, ID> {
    fun findById(id: ID): T?
    fun existsById(id: ID): Boolean
    fun findAll(): List<T>
    fun count(): Long
}
```

### 도메인별 Repository 인터페이스
```kotlin
interface UserRepository : Repository<User, Long> {
    fun findByEmail(email: Email): User?
    fun existsByEmail(email: Email): Boolean
    fun findActiveUsers(): List<User>
    fun findByStatus(status: UserStatus): List<User>
    fun findUsersCreatedAfter(date: LocalDateTime): List<User>
}

// Read-Only Repository (CQRS 패턴)
interface UserQueryRepository : ReadOnlyRepository<User, Long> {
    fun findUserSummaries(pageable: Pageable): Page<UserSummary>
    fun searchUsers(criteria: UserSearchCriteria): List<User>
    fun getUserStatistics(): UserStatistics
}
```

### Repository 구현 패턴
```kotlin
@Repository
@Transactional
class JpaUserRepository(
    private val jpaRepository: SpringDataUserRepository,
    private val queryRepository: UserJpaQueryRepository,
    private val eventPublisher: DomainEventPublisher
) : UserRepository {
    
    override fun save(entity: User): User {
        val savedEntity = jpaRepository.save(entity)
        
        // 도메인 이벤트 발행
        if (savedEntity.getDomainEvents().isNotEmpty()) {
            eventPublisher.publishAll(savedEntity.getDomainEvents())
            savedEntity.clearDomainEvents()
        }
        
        return savedEntity
    }
    
    override fun findByEmail(email: Email): User? {
        return jpaRepository.findByEmail(email.value)
    }
    
    override fun existsByEmail(email: Email): Boolean {
        return jpaRepository.existsByEmail(email.value)
    }
    
    override fun findActiveUsers(): List<User> {
        return jpaRepository.findByStatus(UserStatus.ACTIVE)
    }
    
    // 복잡한 쿼리는 QueryDSL 활용
    override fun findUsersCreatedAfter(date: LocalDateTime): List<User> {
        return queryRepository.findUsersCreatedAfter(date)
    }
}

// Spring Data JPA Repository
interface SpringDataUserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
    fun findByStatus(status: UserStatus): List<User>
}
```

## Domain Service 패턴

### 도메인 서비스 인터페이스
```kotlin
interface DomainService {
    // 마커 인터페이스
}

interface UserDomainService : DomainService {
    fun isEmailUnique(email: Email): Boolean
    fun calculateUserRank(userId: Long): UserRank
    fun canUserPerformAction(userId: Long, action: String): Boolean
}
```

### 도메인 서비스 구현
```kotlin
@Service
class UserDomainServiceImpl(
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository
) : UserDomainService {
    
    override fun isEmailUnique(email: Email): Boolean {
        return !userRepository.existsByEmail(email)
    }
    
    override fun calculateUserRank(userId: Long): UserRank {
        val user = userRepository.findById(userId)
            ?: throw UserNotFoundException("User not found: $userId")
        
        val totalOrderAmount = orderRepository.getTotalOrderAmountByUserId(userId)
        val orderCount = orderRepository.countByUserId(userId)
        
        return when {
            totalOrderAmount >= Money.won(1_000_000) && orderCount >= 10 -> UserRank.VIP
            totalOrderAmount >= Money.won(500_000) && orderCount >= 5 -> UserRank.GOLD
            totalOrderAmount >= Money.won(100_000) && orderCount >= 2 -> UserRank.SILVER
            else -> UserRank.BRONZE
        }
    }
    
    override fun canUserPerformAction(userId: Long, action: String): Boolean {
        val user = userRepository.findById(userId)
            ?: return false
        
        if (!user.isActive()) {
            return false
        }
        
        return when (action) {
            "PLACE_ORDER" -> user.isActive()
            "CANCEL_ORDER" -> user.isActive()
            "REVIEW_PRODUCT" -> user.isActive() && hasOrderHistory(userId)
            else -> false
        }
    }
    
    private fun hasOrderHistory(userId: Long): Boolean {
        return orderRepository.countByUserId(userId) > 0
    }
}

enum class UserRank {
    BRONZE, SILVER, GOLD, VIP
}
```

## Domain Event 패턴

### 도메인 이벤트 인터페이스
```kotlin
interface DomainEvent {
    val occurredOn: LocalDateTime
    val eventId: String
}

abstract class BaseDomainEvent(
    override val occurredOn: LocalDateTime = LocalDateTime.now(),
    override val eventId: String = UUID.randomUUID().toString()
) : DomainEvent
```

### 도메인 이벤트 구현
```kotlin
data class UserCreatedEvent(
    val userId: Long,
    val email: String,
    override val occurredOn: LocalDateTime = LocalDateTime.now(),
    override val eventId: String = UUID.randomUUID().toString()
) : BaseDomainEvent(occurredOn, eventId)

data class UserProfileUpdatedEvent(
    val userId: Long,
    val oldProfile: UserProfile,
    val newProfile: UserProfile,
    override val occurredOn: LocalDateTime = LocalDateTime.now(),
    override val eventId: String = UUID.randomUUID().toString()
) : BaseDomainEvent(occurredOn, eventId)

data class UserActivatedEvent(
    val userId: Long,
    override val occurredOn: LocalDateTime = LocalDateTime.now(),
    override val eventId: String = UUID.randomUUID().toString()
) : BaseDomainEvent(occurredOn, eventId)
```

### 이벤트 발행자
```kotlin
@Component
class DomainEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    
    fun publish(event: DomainEvent) {
        applicationEventPublisher.publishEvent(event)
    }
    
    fun publishAll(events: List<DomainEvent>) {
        events.forEach { publish(it) }
    }
}

// 이벤트 핸들러 예시
@Component
class UserEventHandler(
    private val emailService: EmailService,
    private val auditService: AuditService
) {
    
    @EventListener
    @Async
    fun handle(event: UserCreatedEvent) {
        // 환영 이메일 발송
        emailService.sendWelcomeEmail(event.email)
        
        // 감사 로그 기록
        auditService.log("USER_CREATED", event.userId)
    }
    
    @EventListener
    @Transactional
    fun handle(event: UserProfileUpdatedEvent) {
        // 프로필 변경 이력 저장
        auditService.log("PROFILE_UPDATED", event.userId, mapOf(
            "oldProfile" to event.oldProfile,
            "newProfile" to event.newProfile
        ))
    }
}
```

## Exception 패턴

### 도메인 예외 계층
```kotlin
// 최상위 도메인 예외
abstract class DomainException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

// 비즈니스 규칙 위반
class BusinessRuleViolationException(
    message: String,
    cause: Throwable? = null
) : DomainException(message, cause)

// 엔티티를 찾을 수 없음
class EntityNotFoundException(
    entityType: String,
    identifier: Any
) : DomainException("$entityType not found: $identifier")

// 구체적인 도메인 예외들
class UserNotFoundException(userId: Long) : EntityNotFoundException("User", userId)
class DuplicateEmailException(email: String) : BusinessRuleViolationException("Email already exists: $email")
class InactiveUserException(userId: Long) : BusinessRuleViolationException("User is inactive: $userId")
```

## 패턴 사용 가이드

### 1. Entity vs Value Object 선택 기준
- **Entity**: 고유한 식별자가 있고 생명주기가 있는 객체
- **Value Object**: 값 자체가 중요하고 불변인 객체

### 2. Aggregate 경계 설정
- 하나의 트랜잭션에서 일관성을 보장해야 하는 범위
- 도메인 불변조건이 적용되는 범위
- 보통 1개의 Entity가 Root가 되고 여러 Value Object를 포함

### 3. Repository 패턴 적용
- Aggregate Root에만 Repository 생성
- 도메인 계층에서는 인터페이스만 정의
- 인프라스트럭처 계층에서 구현

### 4. Domain Service 사용 시점
- 여러 Aggregate에 걸친 비즈니스 로직
- 외부 시스템과의 상호작용이 필요한 도메인 로직
- 복잡한 계산이나 정책이 필요한 경우