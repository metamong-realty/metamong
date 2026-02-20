---
description: "테스트 커버리지를 체크하고 부족한 부분을 식별하여 테스트를 추가합니다"
---

프로젝트의 테스트 커버리지를 분석하고 부족한 부분을 식별하여 테스트를 추가하라.

## 절차

### 1. 커버리지 측정

`./gradlew test jacocoTestReport`를 실행하고 리포트(`build/reports/jacoco/test/html/index.html`)를 분석하여 아래 형식으로 보고하라:

```
Overall Coverage: XX.X%
 - Line Coverage: XX.X% (Target: 80%)
 - Branch Coverage: XX.X% (Target: 70%)

Package Coverage:
 - com.metamong.domain.user: XX.X%
 - com.metamong.application.user: XX.X%

Low Coverage Files:
1. SomeService.kt: XX.X%
2. AnotherService.kt: XX.X%
```

### 2. 누락된 테스트 식별

커버되지 않은 코드를 분석하라:
- 테스트가 없는 메서드
- 브랜치(조건문)가 부분적으로만 커버된 코드
- 에러/예외 시나리오가 누락된 코드

### 3. 테스트 생성

누락된 테스트를 BehaviorSpec으로 작성하라. when/if 표현식의 모든 분기에 대한 테스트를 포함하라.

**우선순위:**
1. CRITICAL: 핵심 비즈니스 로직 (결제, 인증, 주문) → 85%+
2. HIGH: 주요 기능 (알림, 검색) → 80%+
3. MEDIUM: 유틸리티/설정 → 60%+

### 4. 재측정

테스트 추가 후 다시 `./gradlew jacocoTestReport`를 실행하여 개선 결과를 보고하라.

## 코드 패턴 참조

- `docs/patterns/testing-patterns.md` — BehaviorSpec, MockK, Fixture, Matcher 패턴

## 목표

- Line Coverage: 80% 이상
- Branch Coverage: 70% 이상
- 신규 코드: 100% 커버
