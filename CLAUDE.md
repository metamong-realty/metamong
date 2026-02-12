# CLAUDE.md - Metamong AI Development Guide

이 문서는 Metamong 프로젝트에서 Claude Code를 최대한 활용하기 위한 AI 최적화 가이드입니다.

## 🎯 최우선 지침

- **모든 답변과 플래닝은 한글로 작성**
- **무조건 동의하지 말고, 다각도로 객관적 분석 후 답변**
- **모호한 요구사항은 추측하지 말고 반드시 질문**
- **복잡한 탐색은 서브에이전트 활용**: `Task(subagent_type="general-purpose", prompt="...")`

---

## ⚡ 워크플로우

```
"기능 구현해줘" → "스펙 작성해줘" → "플랜 작성해줘" → "구현해줘" → "코드리뷰 해줘"
```

**복잡한 탐색은 서브에이전트 활용:**
```
Task(subagent_type="general-purpose", prompt="User 도메인 관련 Entity, Service, Controller를 찾아서 파일 경로와 핵심 메서드를 요약해줘")
```

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
5. **🚫 @Query 애노테이션 사용 금지** - QueryDSL 또는 method naming 사용 필수

### 단계별 작업 가이드
| 단계 | 자동 진행 | 설명 |
|------|----------|------|
| 1 | Spec | 요구사항 명세. 모호한 점 질문 후 spec.md 생성 |
| 2 | Plan | 구현 계획 수립. **사용자 승인 대기** |
| 3 | 구현 | 승인 후 plan 순서대로 구현 |
| 4 | (선택) | "코드리뷰 해줘"로 리뷰 요청 |

**팁**: 코드리뷰는 새 세션에서 하면 더 객관적

## 🏗️ 프로젝트 구조

```
metamong/
├── back/
│   ├── server/          # 메인 API 서버
│   │   ├── domain/      # 도메인 레이어 (Entity, VO)
│   │   ├── application/ # 애플리케이션 레이어 (Service)
│   │   ├── presentation/# 프레젠테이션 레이어 (Controller, DTO)
│   │   └── infra/ # 인프라 레이어 (Repository구현, 외부연동)
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

- **Domain-Driven Design (DDD)** 적용
- **Clean Architecture** 의존성 규칙 준수  
- **SOLID 원칙** 엄격 적용

> 상세 내용은 `architecture` skill 자동 참조

## 🎨 코딩 컨벤션

- **Kotlin 스타일**: Trailing comma, runCatching, requireNotNull 사용
- **네이밍 규칙**: camelCase, PascalCase, snake_case 적절히 사용
- **코드 품질**: 파일 400줄, 함수 30줄 이하 권장

> 상세 내용은 `code-convention` skill 자동 참조

## 🧪 테스트 전략

- **Kotest BehaviorSpec** 사용 (Given-When-Then)
- **테스트 커버리지**: 80% 이상 유지
- **MockK** 활용한 모킹

> 상세 테스트 가이드는 `test-guide` skill 자동 참조

## 🔒 보안 원칙

- **API 보안**: JWT, Rate Limiting, SQL Injection 방지
- **데이터 보안**: 암호화, 마스킹, 감사 로그

> 상세 체크리스트는 `security-checklist` skill 자동 참조

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

## 📊 데이터 액세스

- **🚫 @Query 애노테이션 사용 금지**
- **Method Naming** 또는 **QueryDSL** 사용

> 상세 Repository 패턴은 `rules/architecture.md` 자동 참조

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

## 🛠️ AI 워크플로우 명령어

### 개발 워크플로우
| 요청 예시 | 설명 |
|----------|------|
| "로그인 기능 구현해줘" | 자동으로 spec → plan → 구현 순서 진행 |
| "스펙 작성해줘" | 요구사항 명세 작성 |
| "플랜 작성해줘" | 구현 계획 수립 |
| "코드리뷰 해줘" | 코드 품질 검증 |

### Skills 자동 참조
- `spec`: 요구사항 명세 작성
- `plan`: 구현 계획 수립  
- `implement`: 자동 워크플로우 실행
- `code-review`: 품질 검증

## 📚 참고 자료

### Rules
- `.claude/rules/` - 상세 규칙 문서

### Templates
- `.claude/templates/` - 코드 템플릿

### Workflows
- `.claude/workflows/` - 작업 프로세스

---

**이 문서는 AI와 함께 효율적으로 개발하기 위한 가이드입니다. 모든 개발 작업시 이 문서를 참조하세요.**