---
name: architecture
description: 아키텍처 원칙과 패턴 가이드. 시스템 설계, 도메인 모델링, 의존성 관리 시 참조
---

# 아키텍처 원칙

Metamong 프로젝트의 아키텍처 설계 원칙과 패턴입니다.

## Domain-Driven Design (DDD)

### 핵심 개념

#### Entity (엔티티)
- 고유한 식별자(ID)를 가짐
- 생명주기 동안 동일성 유지
- 비즈니스 로직 포함

```kotlin
@Entity
class Order(
    @Id val id: OrderId,
    val customerId: CustomerId,
    private val items: MutableList<OrderItem> = mutableListOf(),
) {
    fun addItem(product: Product, quantity: Int) {
        // 비즈니스 규칙 검증
        require(quantity > 0) { "수량은 0보다 커야 합니다" }
        items.add(OrderItem(product, quantity))
    }
    
    fun calculateTotal(): Money {
        return items.sumOf { it.calculatePrice() }
    }
}
```

#### Value Object (값 객체)
- 불변 객체
- 속성으로만 식별
- equals/hashCode 구현 필수

```kotlin
data class Money(
    val amount: BigDecimal,
    val currency: Currency,
) {
    init {
        require(amount >= BigDecimal.ZERO) {
            "금액은 음수일 수 없습니다"
        }
    }
    
    operator fun plus(other: Money): Money {
        require(currency == other.currency) {
            "다른 통화끼리 연산할 수 없습니다"
        }
        return Money(amount + other.amount, currency)
    }
}

data class Email(val value: String) {
    init {
        require(value.contains("@")) {
            "유효하지 않은 이메일 형식입니다"
        }
    }
}
```

#### Aggregate (애그리게이트)
- 일관성 경계 정의
- Root Entity를 통해서만 접근
- 트랜잭션 단위

```kotlin
// Aggregate Root
@Entity
class ShoppingCart(
    @Id val id: CartId,
    val customerId: CustomerId,
) {
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    private val items: MutableList<CartItem> = mutableListOf()
    
    // 모든 수정은 Aggregate Root를 통해
    fun addItem(productId: ProductId, quantity: Int) {
        val existingItem = items.find { it.productId == productId }
        if (existingItem != null) {
            existingItem.increaseQuantity(quantity)
        } else {
            items.add(CartItem(productId, quantity))
        }
    }
    
    fun removeItem(productId: ProductId) {
        items.removeIf { it.productId == productId }
    }
}

// Aggregate 내부 Entity
@Entity
class CartItem(
    val productId: ProductId,
    private var quantity: Int,
) {
    internal fun increaseQuantity(amount: Int) {
        quantity += amount
    }
}
```

#### Domain Service
- Entity나 Value Object에 속하지 않는 도메인 로직
- 상태를 가지지 않음

```kotlin
@DomainService
class PricingService {
    fun calculateDiscount(
        order: Order,
        customer: Customer,
        promotions: List<Promotion>,
    ): Money {
        var discount = Money.ZERO
        
        // 고객 등급별 할인
        discount += customer.grade.getDiscount(order.calculateTotal())
        
        // 프로모션 적용
        promotions.forEach { promotion ->
            if (promotion.isApplicable(order)) {
                discount += promotion.calculateDiscount(order)
            }
        }
        
        return discount
    }
}
```

---

## Clean Architecture

### 레이어 구조

```
┌─────────────────────────────────────┐
│         Presentation Layer          │  ← Controllers, DTOs
├─────────────────────────────────────┤
│         Application Layer           │  ← Use Cases, Services
├─────────────────────────────────────┤
│           Domain Layer              │  ← Entities, Value Objects
├─────────────────────────────────────┤
│        Infrastructure Layer         │  ← Repositories, External APIs
└─────────────────────────────────────┘
```

### 의존성 규칙

```
Domain → Application → Infrastructure
         ↓
    Presentation
```

- **내부 레이어는 외부 레이어를 모름**
- **의존성 역전 원칙(DIP) 적용**

### 레이어별 책임

#### Domain Layer
```kotlin
// 순수한 비즈니스 로직만 포함
interface UserRepository {
    fun findById(id: UserId): User?
    fun save(user: User): User
}

class User(
    val id: UserId,
    val email: Email,
    var name: String,
) {
    fun changeName(newName: String) {
        require(newName.isNotBlank()) { "이름은 비어있을 수 없습니다" }
        this.name = newName
    }
}
```

#### Application Layer
```kotlin
// Use Case 구현, 트랜잭션 관리
@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val eventPublisher: EventPublisher,
) {
    fun updateUserName(userId: Long, newName: String): UserDto {
        val user = userRepository.findById(UserId(userId))
            ?: throw UserNotFoundException(userId)
        
        user.changeName(newName)
        val savedUser = userRepository.save(user)
        
        eventPublisher.publish(UserNameChangedEvent(userId, newName))
        
        return savedUser.toDto()
    }
}
```

#### Infrastructure Layer
```kotlin
// 실제 구현체, 외부 시스템 연동
@Repository
class JpaUserRepository(
    private val jpaRepository: UserJpaRepository,
) : UserRepository {
    override fun findById(id: UserId): User? {
        return jpaRepository.findByIdOrNull(id.value)
            ?.toDomainEntity()
    }
    
    override fun save(user: User): User {
        val entity = user.toJpaEntity()
        return jpaRepository.save(entity).toDomainEntity()
    }
}
```

#### Presentation Layer
```kotlin
// HTTP 요청/응답 처리
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {
    @PutMapping("/{id}/name")
    fun updateName(
        @PathVariable id: Long,
        @RequestBody request: UpdateNameRequest,
    ): ResponseEntity<UserResponse> {
        val user = userService.updateUserName(id, request.name)
        return ResponseEntity.ok(user.toResponse())
    }
}
```

---

## SOLID 원칙

### Single Responsibility (단일 책임)
```kotlin
// ✅ 각 클래스는 하나의 책임만
class UserValidator {
    fun validate(user: User) { /* 검증 로직 */ }
}

class UserNotifier {
    fun notify(user: User) { /* 알림 로직 */ }
}

// ❌ 여러 책임을 가진 클래스
class UserManager {
    fun validate(user: User) { }
    fun save(user: User) { }
    fun notify(user: User) { }
}
```

### Open/Closed (개방-폐쇄)
```kotlin
// ✅ 확장에는 열려있고 수정에는 닫혀있음
interface PaymentProcessor {
    fun process(amount: Money)
}

class CreditCardProcessor : PaymentProcessor {
    override fun process(amount: Money) { /* 신용카드 처리 */ }
}

class PayPalProcessor : PaymentProcessor {
    override fun process(amount: Money) { /* PayPal 처리 */ }
}
```

### Liskov Substitution (리스코프 치환)
```kotlin
// ✅ 하위 타입은 상위 타입을 대체 가능
open class Bird {
    open fun fly() { /* 날기 */ }
}

class Sparrow : Bird() {
    override fun fly() { /* 참새의 날기 */ }
}

// ❌ LSP 위반 - 펭귄은 날 수 없음
class Penguin : Bird() {
    override fun fly() {
        throw UnsupportedOperationException("펭귄은 날 수 없습니다")
    }
}
```

### Interface Segregation (인터페이스 분리)
```kotlin
// ✅ 작은 인터페이스로 분리
interface Readable {
    fun read(): String
}

interface Writable {
    fun write(data: String)
}

class File : Readable, Writable {
    override fun read(): String { /* 읽기 */ }
    override fun write(data: String) { /* 쓰기 */ }
}

class ReadOnlyFile : Readable {
    override fun read(): String { /* 읽기만 */ }
}
```

### Dependency Inversion (의존성 역전)
```kotlin
// ✅ 추상화에 의존
interface EmailSender {
    fun send(email: Email)
}

class UserService(
    private val emailSender: EmailSender,  // 인터페이스에 의존
) {
    fun registerUser(user: User) {
        // 비즈니스 로직
        emailSender.send(createWelcomeEmail(user))
    }
}

// 구현체는 인프라 레이어에
class SmtpEmailSender : EmailSender {
    override fun send(email: Email) { /* SMTP 구현 */ }
}
```

---

## 프로젝트 구조

```
metamong/
├── back/
│   ├── server/
│   │   ├── domain/           # 도메인 레이어
│   │   │   ├── user/
│   │   │   │   ├── entity/  # Entity, Value Object
│   │   │   │   ├── repository/  # Repository 인터페이스
│   │   │   │   └── service/  # Domain Service
│   │   │   └── order/
│   │   ├── application/      # 애플리케이션 레이어
│   │   │   ├── user/
│   │   │   │   ├── service/  # Application Service
│   │   │   │   └── dto/      # Application DTO
│   │   │   └── order/
│   │   ├── presentation/     # 프레젠테이션 레이어
│   │   │   ├── user/
│   │   │   │   ├── controller/
│   │   │   │   └── dto/      # Request/Response DTO
│   │   │   └── order/
│   │   └── infrastructure/   # 인프라 레이어
│   │       ├── persistence/
│   │       │   ├── jpa/
│   │       │   └── mongodb/
│   │       ├── messaging/
│   │       └── external/
│   ├── batch/               # 배치 작업
│   └── common/              # 공통 모듈
└── front/                   # 프론트엔드
```

---

## 설계 원칙

### 낮은 결합도, 높은 응집도
- 모듈 간 의존성 최소화
- 관련된 기능은 한 곳에 모음

### 도메인 중심 설계
- 비즈니스 로직이 도메인 레이어에 집중
- 기술적 세부사항은 인프라 레이어로 격리

### 테스트 가능한 설계
- 의존성 주입을 통한 모킹 가능
- 도메인 로직 단위 테스트 용이