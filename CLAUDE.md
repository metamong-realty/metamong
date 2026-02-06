# CLAUDE.md - Metamong AI Development Guide

이 문서는 Metamong 프로젝트에서 Claude Code를 최대한 활용하기 위한 AI 최적화 가이드입니다.

## 🚀 Quick Start

```bash
# 새 도메인 생성
/init-domain user

# TDD로 기능 구현
/tdd "사용자 회원가입 기능"

# API 생성
/api-create POST /api/v1/users

# 보안 검사
/security-audit
```

## 📋 프로젝트 개요

- **기술 스택**: Kotlin 2.0, Spring Boot 3.3, JPA, QueryDSL, MongoDB, Redis
- **아키텍처**: Multi-module DDD, Clean Architecture
- **테스트**: Kotest BehaviorSpec, MockK, Testcontainers
- **품질 관리**: ktlint, JaCoCo (80% coverage), SonarQube

## 🎯 AI 작업 지침

### 필수 준수 사항
1. **모든 코드는 ktlint 규칙 준수** - 작성 후 자동 실행
2. **테스트 커버리지 80% 이상 유지** - 모든 신규 코드
3. **보안 체크리스트 자동 검증** - 커밋 전 필수
4. **DDD 원칙 엄격 적용** - 도메인 경계 명확화

### 작업 워크플로
```
/plan → /tdd → 구현 → /test-coverage → /security-audit → 커밋
```

## 🏗️ 프로젝트 구조

```
metamong/
├── back/
│   ├── server/          # 메인 API 서버
│   │   ├── domain/      # 도메인 레이어 (Entity, VO)
│   │   ├── application/ # 애플리케이션 레이어 (Service)
│   │   ├── presentation/# 프레젠테이션 레이어 (Controller, DTO)
│   │   └── infrastructure/ # 인프라 레이어 (Repository구현, 외부연동)
│   ├── batch/          # 배치 작업
│   └── common/         # 공통 모듈
└── front/              # 프론트엔드 (추후 개발)
```

## 🔧 개발 명령어

### 빌드 & 테스트
```bash
./gradlew build          # 전체 빌드
./gradlew test          # 테스트 실행
./gradlew ktlintFormat  # 코드 포맷팅
./gradlew jacocoTestReport # 커버리지 리포트
./gradlew bootRun       # 서버 실행
```

## 📐 아키텍처 원칙

### 1. Domain-Driven Design (DDD)
- **Entity**: 비즈니스 로직 포함, ID로 식별
- **Value Object**: 불변 객체, equals/hashCode 구현
- **Aggregate**: 일관성 경계, Root Entity를 통한 접근
- **Repository**: 도메인 객체 영속성 관리

### 2. Clean Architecture 의존성 규칙
```
Domain → Application → Infrastructure
         ↓
    Presentation
```
- 내부 레이어는 외부 레이어를 모름
- 의존성 역전 원칙(DIP) 적용

### 3. SOLID 원칙
- **S**ingle Responsibility: 단일 책임
- **O**pen/Closed: 확장 열림, 수정 닫힘
- **L**iskov Substitution: 리스코프 치환
- **I**nterface Segregation: 인터페이스 분리
- **D**ependency Inversion: 의존성 역전

## 🎨 코딩 컨벤션

### Kotlin 스타일
- **네이밍**: camelCase (변수/함수), PascalCase (클래스)
- **불변성**: val 우선 사용, var는 최소화
- **Null Safety**: !! 사용 금지, ?. 또는 ?: 활용
- **함수형**: 고차함수, 람다 적극 활용

### 클래스 구조
```kotlin
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    // 1. 상수
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    
    // 2. 주요 비즈니스 메서드
    fun createUser(request: CreateUserRequest): UserResponse {
        // 구현
    }
    
    // 3. 보조 private 메서드
    private fun validateEmail(email: String) {
        // 구현
    }
}
```

## 🧪 테스트 전략

### 테스트 구조 (Kotest BehaviorSpec)
```kotlin
class UserServiceTest : BehaviorSpec({
    val fixture = kotlinFixture()
    val mockRepository = mockk<UserRepository>()
    val service = UserService(mockRepository)
    
    Given("사용자 생성 요청이 있을 때") {
        val request = fixture<CreateUserRequest>()
        every { mockRepository.save(any()) } returns fixture<User>()
        
        When("유효한 데이터로 요청하면") {
            val result = service.createUser(request)
            
            Then("사용자가 생성된다") {
                result shouldNotBe null
                verify { mockRepository.save(any()) }
            }
        }
    }
})
```

### 테스트 커버리지 목표
- Unit Test: 80% 이상
- Integration Test: 주요 시나리오 100%
- E2E Test: Critical Path 100%

## 🔒 보안 체크리스트

### API 보안
- [ ] JWT 토큰 검증
- [ ] Rate Limiting 적용
- [ ] SQL Injection 방지 (파라미터 바인딩)
- [ ] XSS 방지 (입력값 검증)
- [ ] CSRF 보호

### 데이터 보안
- [ ] 비밀번호 암호화 (BCrypt)
- [ ] 민감정보 마스킹
- [ ] 감사 로그 기록
- [ ] PII 암호화

## 🚦 Git 워크플로

### 브랜치 전략
- `main`: 프로덕션
- `develop`: 개발 통합
- `feature/*`: 기능 개발
- `hotfix/*`: 긴급 수정

### 커밋 메시지
```
[TYPE] 제목

본문 (선택)

TYPE: feat|fix|docs|style|refactor|test|chore
```

## 📊 성능 최적화

### 데이터베이스
- N+1 문제 방지 (fetch join)
- 인덱스 최적화
- 쿼리 캐싱 (Redis)
- Connection Pool 튜닝

### 애플리케이션
- Lazy Loading 활용
- 캐시 전략 (Redis)
- 비동기 처리 (Coroutine)
- GC 튜닝

## 🔍 모니터링

### 로깅
```kotlin
logger.info { "사용자 생성: $userId" }
logger.error(e) { "에러 발생" }
```

### 메트릭
- API 응답 시간
- 에러율
- 처리량 (TPS)
- JVM 메모리

## 🛠️ AI 명령어 레퍼런스

### 도메인 개발
- `/init-domain [name]` - 새 도메인 초기화
- `/add-entity [domain] [entity]` - 엔티티 추가
- `/add-repository [entity]` - 리포지토리 생성

### API 개발
- `/api-create [method] [path]` - API 엔드포인트 생성
- `/add-validation [dto]` - DTO 검증 추가
- `/add-swagger [controller]` - Swagger 문서화

### 테스트
- `/tdd [feature]` - TDD 워크플로 시작
- `/test-coverage` - 커버리지 체크
- `/add-test [class]` - 테스트 추가

### 품질 관리
- `/security-audit` - 보안 검사
- `/performance-check` - 성능 분석
- `/code-review` - 코드 리뷰

## 📚 참고 자료

### Rules
- `.claude/rules/` - 상세 규칙 문서

### Templates
- `.claude/templates/` - 코드 템플릿

### Workflows
- `.claude/workflows/` - 작업 프로세스

---

**이 문서는 AI와 함께 효율적으로 개발하기 위한 가이드입니다. 모든 개발 작업시 이 문서를 참조하세요.**