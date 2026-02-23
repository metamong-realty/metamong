---
paths: "**/repository/**/*.kt, **/infra/**/*.kt, **/persistence/**/*.kt, **/entity/**/*.kt"
---

# Database Guidelines

## JPA Entity 규칙

- **테이블명**: lower_snake_case, 복수형 / **Entity명**: PascalCase, 단수형
- **@Enumerated(EnumType.STRING)** 필수 (ORDINAL 금지)
- **연관관계 매핑 금지** — QueryDSL로 JOIN 처리
- **@Column**: 이름이 다를 때만 사용, 제약조건은 DB에서 관리
- Auditing: `BaseEntity` 상속 (`@CreatedDate`, `@LastModifiedDate`)
- **금액 필드는 `BigDecimal` 사용** (`Long`/`Int` 금지) — DDL: `DECIMAL(15,0)` 또는 적절한 precision

## Repository 메서드 중복 방지 (필수)

- **새 쿼리 메서드 작성 전, 반드시 같은 도메인의 Repository/RepositoryCustom/RepositoryCustomImpl 파일을 Read로 확인**
- 유사한 쿼리가 이미 있으면 재사용 (파라미터 추가로 확장 가능한 경우 포함)
- 단, 기존 메서드의 시그니처/반환타입 변경은 금지 (기존 호출측 영향 방지)
- 새 메서드가 필요한 경우에만 추가

## QueryDSL 사용법

- **@Query 애노테이션 사용 금지** — QueryDSL 또는 method naming만 사용
- JPA Repository: 단순 CRUD, method naming 쿼리
- QueryDSL Repository: 복잡한 조회, 동적 쿼리, Projection
- `BooleanBuilder`로 동적 쿼리 구성

## N+1 문제 방지

- 기본 Lazy Loading, 필요시 Fetch Join
- 연관 데이터: ID 목록으로 Batch 조회 후 `associateBy`로 매핑

## 트랜잭션 관리

- Service 클래스에 `@Transactional` 선언
- 조회 메서드는 `@Transactional(readOnly = true)`
- 트랜잭션 범위 최소화
- 독립 트랜잭션: `Propagation.REQUIRES_NEW`
- 커밋 후 처리: `@TransactionalEventListener(phase = AFTER_COMMIT)`

## 기타

- **배치 처리**: Spring Batch Writer에서는 JDBC batch insert 사용. 일반 Service에서는 JPA `saveAll` + `batch_size`
- **인덱스**: `@Table(indexes = [...])`, 자주 조회하는 컬럼에 설정
- **마이그레이션**: Flyway, `V{번호}__{설명}.sql` 네이밍
- **Connection Pool**: HikariCP, `application.yml`에서 관리

## 코드 패턴 참조

- `docs/patterns/querydsl-patterns.md` — 동적 쿼리, Projection, Batch 조회 예시
- `docs/patterns/domain-patterns.md` — Entity, BaseEntity 구현 예시

## 체크리스트

- [ ] `@Enumerated(EnumType.STRING)` 사용
- [ ] 연관관계 매핑 대신 QueryDSL 사용
- [ ] N+1 문제 체크 (Fetch Join / Batch 조회)
- [ ] 인덱스 적절히 설정
- [ ] 트랜잭션 범위 최소화, 읽기 전용 분리
- [ ] Projection 활용 (불필요한 컬럼 조회 방지)
- [ ] `@Query` 애노테이션 미사용 확인
