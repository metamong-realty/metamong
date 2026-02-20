---
globs: "**/*.kt"
---

# Architecture Guidelines

## DDD (Domain-Driven Design) 원칙

### Entity
- 비즈니스 로직 포함, ID로 식별, Setter 사용 금지
- 상태 변경은 도메인 메서드를 통해서만

```kotlin
@Entity
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var email: String,
    private var password: String
) {
    fun changePassword(newPassword: String) {
        require(newPassword.length >= 8) { "비밀번호는 8자 이상" }
        this.password = newPassword
    }
}
```

### Value Object
- 불변 객체 (val only), data class 활용, 자체 검증 포함

```kotlin
@Embeddable
data class Email(val value: String) {
    init { require(value.contains("@")) { "올바른 이메일 형식이 아닙니다" } }
}
```

### Aggregate
- Root Entity를 통해서만 접근
- 트랜잭션 경계와 일치
- 다른 Aggregate는 ID로만 참조

### Repository
- Domain 레이어에 인터페이스, Infra 레이어에 구현
- Aggregate 단위로 생성

```kotlin
// Domain Layer - 인터페이스
interface UserRepository {
    fun save(user: User): User
    fun findById(id: Long): User?
}

// Infra Layer - 구현
@Repository
class UserRepositoryImpl(
    private val jpaRepository: UserJpaRepository
) : UserRepository {
    override fun save(user: User) = jpaRepository.save(user)
    override fun findById(id: Long) = jpaRepository.findByIdOrNull(id)
}
```

### 배치 처리 최적화
- 기본: JPA `saveAll` + `batch_size: 100` 설정으로 충분
- JDBC Repository는 특수 요구사항이 있을 때만 사용
- JPA가 ID 자동 할당하므로 재조회 불필요

## Clean Architecture 레이어

### 의존성 방향
```
Domain (중심) <-- Application <-- Infra / Presentation
```

| 레이어 | 역할 | 포함 요소 |
|--------|------|-----------|
| **Domain** | 비즈니스 로직, 프레임워크 독립, 순수 Kotlin | Entity, VO, Repository 인터페이스 |
| **Application** | Use Case 구현, 트랜잭션 관리 | Service, DTO, Command |
| **Infra** | 외부 시스템 연동, DB 구현 | JPA 구현체, 메시징, 캐시 |
| **Presentation** | REST API, 입력 검증 | Controller, Request/Response DTO |

## SOLID 원칙

- **SRP**: 클래스/메서드는 하나의 책임만, 변경 이유 하나
- **OCP**: 확장에 열림, 수정에 닫힘 (인터페이스/Strategy 패턴)
- **LSP**: 하위 타입은 상위 타입 대체 가능
- **ISP**: 클라이언트별 인터페이스 분리, 작은 인터페이스 선호
- **DIP**: 추상화에 의존, 구체 클래스 의존 금지

```kotlin
// Good - 인터페이스 의존
class OrderService(private val paymentGateway: PaymentGateway)

// Bad - 구체 클래스 의존
class OrderService(private val iamportPayment: IamportPayment)
```

## 패키지 구조

```
com.metamong/
├── domain/              # Entity, VO, Repository 인터페이스, Domain Service
├── application/         # Application Service, DTO, Command
├── infra/               # JPA 구현, 메시징, 외부 API
└── presentation/        # Controller, Request/Response DTO
```

## 설계 원칙

- 생성자 주입으로 불변성 보장
- 모든 의존성은 주입 가능 (테스트 용이)
- 비즈니스 로직과 인프라 분리
- 기본 Lazy Loading, N+1은 Fetch Join으로 해결
- Redis 캐싱, Coroutine 비동기 처리

## 체크리스트

- [ ] DDD 도메인 경계 명확화
- [ ] Clean Architecture 의존성 방향 준수
- [ ] SOLID 원칙 적용
- [ ] Repository: Domain 인터페이스 + Infra 구현 분리
- [ ] 배치 처리: JPA saveAll 우선 사용
- [ ] 인터페이스 의존, 생성자 주입
- [ ] 테스트 가능한 설계 (Mock 가능한 인터페이스)
