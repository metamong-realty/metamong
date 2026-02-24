---
description: "자동으로 spec → plan → 구현 순서로 진행하는 워크플로우입니다"
---

spec → plan → 구현을 자동으로 이어서 진행하는 워크플로우 스킬이다.

## 절차

### 0단계 : 워크플로우 진행 여부 확인
- skill이 실행되면, 먼저 사용자에게 워크플로우 진행 여부를 확인하라
- 예: "이 작업은 새 Service 생성 + API 3개가 필요한 복잡한 작업입니다. spec → plan → 구현 순서로 진행할까요, 아니면 바로 구현할까요?"

### 1단계: Spec 생성
- 관련 파일을 서브에이전트(`Task(subagent_type="general-purpose")`)로 탐색하라.
- 모호한 요구사항은 사용자에게 질문하라.
- `.claude/planning/{브랜치명}-spec.md`를 생성하라.

### 2단계: Plan 생성
- spec 기반으로 구현 계획을 수립하라.
- `.claude/planning/{브랜치명}-plan.md`를 생성하라.
- **사용자 승인을 반드시 받아라. 승인 없이 구현을 시작하지 마라.**

### 3단계: 구현
승인 후, plan의 체크리스트 순서대로 진행하라:
- DDD 및 Clean Architecture 준수
- 테스트 코드 작성 (80% 커버리지)
- ktlint 규칙 적용
- @Query 금지 → QueryDSL 또는 method naming 사용

## 구현 시 준수 사항

- **코드 품질**: ktlint, SOLID, Clean Architecture 의존성 방향
- **테스트**: Kotest BehaviorSpec, Given-When-Then, MockK
- **보안**: 입력값 검증, SQL Injection 방지, 인증/인가

## 완료 후

구현이 끝나면 사용자에게 안내하라:
- `/code-review`로 품질 검증 (권장)
