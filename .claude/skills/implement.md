---
name: implement
description: 기능 구현. "구현해줘" 요청 시 자동으로 spec → plan → 구현 순서 진행
---

# Implement (구현)

자동으로 spec → plan → 구현 순서로 진행하는 워크플로우입니다.

## 자동 워크플로우

```
사용자: "로그인 기능 구현해줘"
  ↓
1. spec 스킬 자동 호출 → spec.md 생성
  ↓
2. plan 스킬 자동 호출 → plan.md 생성 및 승인 대기
  ↓
3. 승인 후 → 실제 구현 진행
```

## 실행 프로세스

### 1단계: 자동 Spec 생성
- 모호한 요구사항은 질문
- 관련 파일 탐색 (서브에이전트 활용)
- `.claude/planning/{브랜치명}-spec.md` 생성

### 2단계: 자동 Plan 생성
- spec 기반 구현 계획 수립
- `.claude/planning/{브랜치명}-plan.md` 생성
- **사용자 승인 대기 (필수)**

### 3단계: 구현 진행
- plan의 체크리스트 순서대로 진행
- 각 단계별 코드 작성
- DDD 및 Clean Architecture 준수
- 테스트 코드 작성 (80% 커버리지)

## 구현 원칙

### 코드 품질
- ktlint 규칙 자동 적용
- @Query 애노테이션 사용 금지
- QueryDSL 또는 method naming 사용

### 테스트 전략
- Kotest BehaviorSpec 사용
- Given-When-Then 구조
- MockK으로 의존성 모킹

### 보안 체크리스트
- 입력값 검증
- SQL Injection 방지
- 인증/인가 처리

## 완료 후

구현 완료 시:
- "코드리뷰 해줘"로 품질 검증 (권장)
- 또는 직접 커밋 진행