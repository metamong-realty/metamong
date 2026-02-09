---
name: spec
description: 요구사항 명세 작성. 새 기능 개발 시작 시 사용
---

# Spec (요구사항 명세)

기능 요구사항을 명확하게 정의합니다.

## 실행 프로세스

1. **코드베이스 탐색**
   - `Task(subagent_type="general-purpose")`로 관련 파일 탐색
   - 메인 컨텍스트 오염 없이 결과만 반환

   ```
   Task(subagent_type="general-purpose", prompt="User 도메인 관련 Entity, Service, Controller를 찾아서 파일 경로와 핵심 메서드를 요약해줘")
   ```

2. **요구사항 명확화**
   - 모호한 점은 **반드시 질문**하고 확인
   - 추측하지 않음

3. **spec.md 생성**
   - `.claude/planning/{브랜치명}-spec.md` 에 저장

## Spec 문서 형식

```markdown
# [기능명] Spec

## 요구사항
- 핵심 기능 설명
- 상세 동작 정의

## 관련 파일
- 수정 대상 파일 목록

## 결정 사항
- 논의 후 결정된 내용

## 제외 사항
- 이번 작업에서 하지 않는 것
```

## 다음 단계

spec 작성 완료 후:
- "플랜 작성해줘"로 구현 계획 수립 (권장)
- 또는 현재 세션에서 계속 진행