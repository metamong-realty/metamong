# CLAUDE.md - Metamong AI Development Guide

이 문서는 Metamong 프로젝트에서 Claude Code를 최대한 활용하기 위한 AI 최적화 가이드입니다.

## 최우선 지침

- **모든 답변과 플래닝은 한글로 작성**
- **무조건 동의하지 말고, 다각도로 객관적 분석 후 답변**
- **모호한 요구사항은 추측하지 말고 반드시 질문**
- **복잡한 탐색은 서브에이전트 활용**: `Task(subagent_type="general-purpose", prompt="...")`

---

## 워크플로우

```
"기능 구현해줘" → /spec → /plan → /implement → /code-review
```

## Quick Start

```bash
# 워크플로우 커맨드
/spec                          # 요구사항 명세 작성
/plan                          # 구현 계획 수립 (승인 대기)
/implement                     # spec → plan → 구현 자동 진행
/code-review                   # 코드 품질 검증

# 도메인/기능 생성
/init-domain user              # 새 도메인 전체 구조 생성
/tdd "사용자 회원가입 기능"       # TDD 사이클로 구현
/api-create POST /api/v1/users # API 엔드포인트 생성

# 품질 관리
/security-audit                # 보안 취약점 스캔
/test-coverage                 # 테스트 커버리지 체크
```

## 프로젝트 개요

### Backend (back/)
- **기술 스택**: Kotlin 2.0, Spring Boot 3.3, JPA, QueryDSL, MongoDB, Redis
- **아키텍처**: Multi-module DDD, Clean Architecture
- **테스트**: Kotest BehaviorSpec, MockK, Testcontainers
- **품질 관리**: ktlint, JaCoCo (80% coverage), SonarQube

### Frontend (front/)
- **기술 스택**: Next.js 16, React 19, TypeScript, Tailwind CSS 4
- **UI**: shadcn/ui (New York), lucide-react
- **상태 관리**: TanStack Query (서버 상태), nuqs (URL 상태)
- **차트**: Chart.js + react-chartjs-2
- **품질 관리**: ESLint, Prettier (저장 시 자동 포맷팅 hook)
- **참고 문서**: `docs/front-migration-guide.md` (마이그레이션 가이드 + 원본 코드)

## 필수 준수 사항

### Backend
1. **모든 코드는 ktlint 규칙 준수** - 저장 시 자동 포맷팅 (hook)
2. **테스트 커버리지 80% 이상 유지** - 모든 신규 코드
3. **DDD 원칙 엄격 적용** - 도메인 경계 명확화
4. **@Query 애노테이션 사용 금지** - QueryDSL 또는 method naming 사용 필수
5. **Clean Architecture 의존성 방향** - Domain ← Application ← Infra/Presentation

### Frontend
1. **Prettier 자동 포맷팅** - 저장 시 자동 실행 (hook)
2. **TypeScript strict 모드** - `any` 사용 금지
3. **shadcn/ui 컴포넌트 우선** - 직접 HTML 구현 금지
4. **API 호출은 커스텀 훅으로** - `hooks/use-*.ts` 패턴
5. **서버 상태는 TanStack Query** - `useState`로 API 데이터 관리 금지

## 프로젝트 구조

```
metamong/
├── back/
│   ├── server/          # 메인 API 서버
│   │   ├── domain/      # 도메인 레이어 (Entity, VO)
│   │   ├── application/ # 애플리케이션 레이어 (Service)
│   │   ├── presentation/# 프레젠테이션 레이어 (Controller, DTO)
│   │   └── infra/       # 인프라 레이어 (Repository구현, 외부연동)
│   ├── batch/           # 배치 작업
│   └── common/          # 공통 모듈
├── front/               # 프론트엔드 (Next.js)
│   └── src/
│       ├── app/         # App Router (layout, page, 동적 라우트)
│       ├── components/  # 컴포넌트 (ui/는 shadcn)
│       ├── hooks/       # 커스텀 훅 (API 쿼리 등)
│       ├── lib/         # 유틸 (api-client, format, utils)
│       └── types/       # 타입 정의
└── docs/
    └── front-migration-guide.md  # FE 마이그레이션 가이드 + 원본 코드 참조
```

## 개발 명령어

### Backend
```bash
./gradlew build          # 전체 빌드
./gradlew test           # 테스트 실행
./gradlew ktlintFormat   # 코드 포맷팅
./gradlew jacocoTestReport # 커버리지 리포트
./gradlew bootRun        # 서버 실행
```

### Frontend
```bash
cd front
npm run dev              # 개발 서버 (localhost:3000)
npm run build            # 프로덕션 빌드
npm run lint             # ESLint 검사
npx prettier --write .   # 전체 포맷팅
npx shadcn@latest add [컴포넌트명]  # shadcn 컴포넌트 추가
```

## Git 워크플로

### 브랜치 전략
- `main`: 프로덕션
- `develop`: 개발 통합
- `feature/*`: 기능 개발
- `hotfix/*`: 긴급 수정

### 커밋 메시지
```
[TYPE]: [TICKET-NUMBER] 제목

본문 (선택)

TYPE: feat|fix|docs|style|refactor|test|chore
```

## 참조 구조

### Skills (슬래시 커맨드)
`.claude/skills/` - 워크플로우 및 코드 생성 커맨드:

| 커맨드 | 설명 |
|--------|------|
| `/spec` | 요구사항 명세 작성 |
| `/plan` | 구현 계획 수립 (승인 대기) |
| `/implement` | spec → plan → 구현 자동 워크플로우 |
| `/code-review` | 코드 품질 5가지 관점 검증 |
| `/tdd "<기능>"` | TDD RED-GREEN-REFACTOR 사이클 |
| `/init-domain <name>` | 도메인 전체 레이어 구조 생성 |
| `/api-create <method> <path>` | RESTful API 엔드포인트 생성 |
| `/security-audit` | OWASP Top 10 보안 취약점 스캔 |
| `/test-coverage` | 테스트 커버리지 분석 및 개선 |
| `/auto-commit` | 변경사항 자동 분리 커밋 (BE/FE 공용) |

### Agents (전문가 서브에이전트)
`.claude/agents/` - Skills에서 참조 가능한 전문가 에이전트:
- `domain-architect.md` - DDD, Clean Architecture 설계
- `test-specialist.md` - Kotest, MockK, 테스트 전략
- `security-reviewer.md` - OWASP Top 10, Spring Security
- `querydsl-expert.md` - QueryDSL, JPA 쿼리 최적화
- `kotlin-expert.md` - Kotlin 컨벤션, 함수형 프로그래밍

### Rules (조건부 자동 로드)
`.claude/rules/` 내 파일들은 globs 패턴에 따라 자동으로 로드됩니다:

**Backend:**
- `architecture.md` - DDD, Clean Architecture, SOLID (`**/*.kt`)
- `api-design.md` - RESTful API, Controller, DTO (`**/controller/**/*.kt`, `**/presentation/**/*.kt`)
- `database.md` - JPA, QueryDSL, 트랜잭션 (`**/repository/**/*.kt`, `**/infra/**/*.kt`, `**/persistence/**/*.kt`)
- `kotlin-style.md` - 네이밍, 코드 스타일 (`**/*.kt`)
- `security.md` - 보안, JWT, 입력 검증 (`**/security/**/*.kt`, `**/auth/**/*.kt`)
- `testing.md` - Kotest, MockK, 테스트 패턴 (`**/*Test*.kt`, `**/*Spec*.kt`, `**/test/**/*.kt`)

**Frontend:**
- `frontend-style.md` - React/TS/Tailwind 컨벤션 (`front/**/*.tsx`, `front/**/*.ts`)
- `nextjs-patterns.md` - App Router, Server/Client Component (`front/src/app/**/*.tsx`)
- `component-design.md` - 컴포넌트 설계, shadcn 활용 (`front/src/components/**/*.tsx`)

### 패턴 라이브러리 (필요시 Read로 참조)
`docs/patterns/`에 상세 코드 패턴과 예시가 보관되어 있습니다:
- `domain-patterns.md` - DDD 도메인 모델링 패턴
- `querydsl-patterns.md` - QueryDSL 쿼리 패턴
- `testing-patterns.md` - 테스트 데이터, Fixture, Matcher 패턴
- `kotlin-extensions.md` - Kotlin 확장 함수 패턴
- `security-patterns.md` - 보안 구현 패턴
- `performance.md` - 성능 최적화 가이드
- `git-workflow.md` - Git 워크플로 상세 가이드
