# Architecture Guidelines

## DDD (Domain-Driven Design) 원칙

### Entity
- 비즈니스 로직 포함, ID로 식별, Setter 사용 금지
- 상태 변경은 도메인 메서드를 통해서만

### Value Object
- 불변 객체 (val only), data class 활용, 자체 검증 포함

### Aggregate
- Root Entity를 통해서만 접근
- 트랜잭션 경계와 일치
- 다른 Aggregate는 ID로만 참조

### Repository
- Domain 레이어에 인터페이스, Infra 레이어에 구현
- Aggregate 단위로 생성

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

## 패키지 구조

```
com.metamong/
├── domain/              # Entity, VO, Repository 인터페이스, Domain Service
├── application/         # Application Service, DTO, Command
├── infra/               # JPA 구현, 메시징, 외부 API
└── presentation/        # Controller, Request/Response DTO
```

## Service 레이어 규칙

- **Entity는 Service 밖으로 노출 금지** → ResponseDto로 변환하여 반환
- Request → Dto 변환 메서드: `toDto()` (`toCommand()` 사용 안함)
- Dto 네이밍: `Create{Domain}RequestDto`, `Update{Domain}RequestDto` (`Command` 접미사 사용 안함)
- Query(조회)와 Command(변경) Service 분리 권장

## 설계 원칙

- 생성자 주입으로 불변성 보장, 모든 의존성은 주입 가능 (테스트 용이)
- 비즈니스 로직과 인프라 분리
- 기본 Lazy Loading, N+1은 Fetch Join으로 해결

## 로깅 규칙

- Logger: `KotlinLogging.logger {}` 사용
- 레벨: ERROR (장애), WARN (주의 필요), INFO (비즈니스 이벤트), DEBUG (개발용)
- 민감정보(비밀번호, 토큰, 개인정보) 로깅 절대 금지
- 예외 로깅 시 스택트레이스 포함: `logger.error(e) { "메시지" }`

## 환경 설정 규칙

- 프로파일: `local`, `dev`, `staging`, `prod` 분리
- 시크릿/크리덴셜: 환경변수 또는 Secrets Manager에서 주입 (`@Value`)
- application.yml에 하드코딩 금지
- 프로파일별 설정: `application-{profile}.yml`

## 코드 패턴 참조

- `docs/patterns/domain-patterns.md` — Entity, VO, Repository, Aggregate 구현 예시

## 체크리스트

- [ ] DDD 도메인 경계 명확화
- [ ] Clean Architecture 의존성 방향 준수
- [ ] SOLID 원칙 적용
- [ ] Repository: Domain 인터페이스 + Infra 구현 분리
- [ ] 인터페이스 의존, 생성자 주입
- [ ] 테스트 가능한 설계 (Mock 가능한 인터페이스)
