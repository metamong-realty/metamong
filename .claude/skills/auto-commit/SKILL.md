---
description: "변경사항을 분석하여 작업별로 자동 분리 커밋합니다. Use when user says 'auto-commit', '자동 커밋', '커밋해줘', 'commit changes'."
---

# /auto-commit - 자동 분리 커밋

변경사항을 분석하여 작업 단위로 자동 분리 커밋합니다.

## 사용법

```
/auto-commit              # 기본: 자동 분리 커밋
/auto-commit --push       # 커밋 후 자동 push
/auto-commit --dry-run    # 커밋 메시지 미리보기만
/auto-commit --single     # 단일 커밋으로 통합
```

## 실행 프로세스

### 1단계: 사전 품질 검사

변경된 파일 타입에 따라 자동 실행:

```bash
# Kotlin 파일이 변경된 경우
./gradlew ktlintFormat --quiet 2>/dev/null || true

# TypeScript/React 파일이 변경된 경우 (front/)
cd front && npx prettier --write $(git diff --name-only -- '*.tsx' '*.ts') 2>/dev/null || true
```

lint/format 결과 새로운 변경이 생기면 해당 파일도 함께 커밋에 포함한다.

### 2단계: 변경사항 분석

```bash
git status
git diff --stat
git diff --name-only
```

### 3단계: 그룹화

변경된 파일들을 다음 기준으로 그룹화:

**파일 경로 기반:**
- `back/server/domain/` → 도메인 변경
- `back/server/presentation/` → API 변경
- `front/src/components/` → 컴포넌트 변경
- `front/src/hooks/` → 훅 변경
- `front/src/app/` → 페이지 변경
- `.claude/` → Claude 설정 변경

**변경 타입 기반:**
- 신규 파일 → `feat`
- 수정 파일 → `feat` 또는 `fix` 또는 `refactor`
- 삭제 파일 → `refactor` 또는 `chore`

**기능 연관성:**
- 같은 도메인/기능에 속하는 파일들은 하나의 커밋으로 묶기
- 예: `use-regions.ts` + `region-selector.tsx` → 하나의 커밋

### 4단계: 커밋 메시지 생성

Conventional Commits 형식:

```
TYPE: 제목 (한글, 간결하게)

- 상세 변경 내용 1
- 상세 변경 내용 2

TYPE: feat|fix|docs|style|refactor|test|chore
```

**규칙:**
- 제목은 한글, 50자 이내
- 본문은 변경 내용을 구체적으로 기술
- Co-Authored-By 태그 포함하지 않음
- `--lang en` 옵션 시 영문 메시지

### 5단계: 실행

각 그룹별로 순차적으로:

```bash
# 1. 해당 그룹의 파일만 스테이징
git add [파일목록]

# 2. 커밋
git commit -m "커밋 메시지"
```

`--push` 옵션이 있으면 모든 커밋 완료 후:
```bash
git push origin $(git branch --show-current)
```

### 6단계: 결과 보고

```
✅ 커밋 완료!

1. feat: 지역 선택 훅 및 컴포넌트 구현
   - src/hooks/use-regions.ts (신규)
   - src/components/region-selector.tsx (신규)

2. feat: 아파트 단지 목록 페이지 구현
   - src/app/page.tsx (수정)
   - src/components/complex-card.tsx (신규)

총 2개 커밋 생성
```

## 주의사항

- `.env`, `.env.local` 등 환경 파일은 커밋하지 않음
- `node_modules/`, `.next/`, `build/` 무시
- `--dry-run` 시 실제 커밋하지 않고 메시지만 표시
- lint/format 에러가 있으면 경고 후 계속 진행 (중단하지 않음)
