---
globs: "**/repository/**/*.kt,**/infra/**/*.kt,**/persistence/**/*.kt"
---

# Database Guidelines

## JPA Entity 규칙

- **테이블명**: lower_snake_case, 복수형 / **Entity명**: PascalCase, 단수형
- **@Enumerated(EnumType.STRING)** 필수 (ORDINAL 금지)
- **연관관계 매핑 금지** - QueryDSL로 JOIN 처리
- **@Column**: 이름이 다를 때만 사용, 제약조건은 DB에서 관리
- Auditing: `BaseEntity` 상속 (`@CreatedDate`, `@LastModifiedDate`)
- Setter 사용 금지 - 상태 변경 메서드 제공

```kotlin
@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val email: String,
    var nickname: String,
    @Enumerated(EnumType.STRING)
    val status: UserStatus = UserStatus.ACTIVE,
    @CreatedDate val createdAt: LocalDateTime = LocalDateTime.now(),
    @LastModifiedDate var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    // 연관관계 매핑 금지 - QueryDSL로 처리
}
```

## QueryDSL 사용법

- **@Query 애노테이션 사용 금지** - QueryDSL 또는 method naming 사용
- JPA Repository: 단순 CRUD, method naming 쿼리
- QueryDSL Repository: 복잡한 조회, 동적 쿼리, Projection
- `BooleanBuilder`로 동적 쿼리 구성

```kotlin
// 동적 쿼리 + Projection 예시
@Repository
class UserQueryRepositoryImpl : UserQueryRepository, QuerydslRepositorySupport(User::class.java) {
    private val user = QUser.user

    override fun searchUsers(keyword: String?, status: UserStatus?, pageable: Pageable): Page<User> {
        val builder = BooleanBuilder()
        keyword?.let { builder.and(user.nickname.contains(it).or(user.email.contains(it))) }
        status?.let { builder.and(user.status.eq(it)) }
        return findAll(builder, pageable)
    }
}
```

## N+1 문제 방지

- 기본 로딩은 Lazy, 필요시 Fetch Join 사용
- 연관 데이터 조회시 ID 목록으로 Batch 조회 후 `associateBy`로 매핑
- QueryDSL에서 `.fetchJoin()` 활용

```kotlin
// Batch 조회 패턴
val boards = boardRepository.findAll()
val authors = userRepository.findAllById(boards.map { it.authorId }.distinct()).associateBy { it.id }
boards.map { BoardDto(it, authors[it.authorId]!!) }
```

## 트랜잭션 관리

- Service 클래스에 `@Transactional` 선언
- 조회 메서드는 `@Transactional(readOnly = true)` 사용
- 트랜잭션 범위 최소화
- 독립 트랜잭션 필요시 `Propagation.REQUIRES_NEW`
- 커밋 후 처리: `@TransactionalEventListener(phase = AFTER_COMMIT)`

## 기타 참고

- **배치 처리**: JPA `saveAll` + `application.yml`의 `batch_size` 설정으로 최적화 (JDBC 직접 사용은 특수 경우만)
- **인덱스**: `@Table(indexes = [...])` 활용, 자주 조회하는 컬럼에 설정
- **마이그레이션**: Flyway 사용, `V{번호}__{설명}.sql` 네이밍
- **캐싱**: Redis `@Cacheable`/`@CacheEvict` 활용 (상세는 별도 가이드)
- **Connection Pool**: HikariCP 설정은 `application.yml`에서 관리

## 체크리스트

- [ ] Entity에 `@Enumerated(EnumType.STRING)` 사용
- [ ] 연관관계 매핑 대신 QueryDSL 사용
- [ ] N+1 문제 체크 (Fetch Join / Batch 조회)
- [ ] 인덱스 적절히 설정
- [ ] 트랜잭션 범위 최소화, 읽기 전용 분리
- [ ] Projection 활용 (불필요한 컬럼 조회 방지)
- [ ] 페이징 처리 (커서 기반 페이징 고려)
- [ ] `@Query` 애노테이션 미사용 확인
