---
description: "커밋을 논리 단위로 분리하고 PR을 자동 생성합니다. 사용자가 'PR', 'PR 만들어줘', 'PR 올려줘'라는 단어를 언급하면 생성 요청인지 확인 후 실행."
allowed-tools: Bash, Read, Write, Edit
---

# create-pr — 커밋 분리 + PR 자동 생성

현재 브랜치의 변경사항을 논리 단위로 커밋 분리하고 PR을 생성한다.

## 절차

### 1단계: 작업 내역 확인

```bash
CURRENT_BRANCH=$(git branch --show-current)
TICKET=$(echo "$CURRENT_BRANCH" | grep -oE 'META-[0-9]+' | head -1)

# base 브랜치 결정
# - FE 변경 → main
# - BE server 변경 → back-deploy
# - BE batch 변경 → back-batch-deploy
# (변경 파일 경로로 자동 판단)

git log origin/main..HEAD --oneline
git diff origin/main...HEAD --stat
```

변경사항을 사용자에게 보여주고 확인하라.

### 2단계: 사전 품질 검사 (필수 — 실패 시 중단)

변경된 파일 타입에 따라 자동 실행:

```bash
# Kotlin 파일 변경 시
cd ~/projects/metamong
./gradlew :back:server:ktlintFormat --no-daemon -q
./gradlew :back:server:ktlintCheck :back:server:build --no-daemon
# 실패 시 → PR 생성 중단, 에러 보고

# Batch 파일 변경 시
./gradlew :back:batch:ktlintFormat --no-daemon -q
./gradlew :back:batch:ktlintCheck :back:batch:build --no-daemon

# FE 파일 변경 시
cd ~/projects/metamong/front && npm run build
```

ktlintFormat으로 변경된 파일이 있으면 스테이징에 포함한다.

### 3단계: 커밋 분리

하나의 커밋 = 하나의 의도(Why):
- 포맷팅/style은 전용 커밋으로 격리 (`style:`)
- 테스트는 구현 직후 커밋 (`test:`)
- 문서/설정 변경 분리 (`docs:`, `chore:`)

커밋 메시지 형식:
```
{type}: [{TICKET}] {간략한 설명}
```

### 4단계: PR 생성

**base 브랜치 자동 판단:**
- `front/` 변경 → `main`
- `back/server/` 변경 → `back-deploy`
- `back/batch/` 변경 → `back-batch-deploy`
- FE + BE 혼재 → 사용자에게 확인

**PR 제목:**
```
{type}: [{TICKET}] {설명}
```

**PR 본문 (비즈니스 관점으로 작성):**
```markdown
## 개요

{배경 + 기존 문제 + 해결 방법}

## 변경사항

- {기술적 설명 말고 비즈니스/사용자 관점으로}
- ❌ "Service 로직 변경, Repository 메서드 추가"
- ✅ "로그인 시 refresh token을 httpOnly cookie에 저장하여 XSS 방어"

## 테스트 결과

{빌드/테스트 통과 여부}

## Before / After (UI 변경 시)

스크린샷 포함
```

**PR 생성 방법 (gh CLI 없을 경우 GitHub API 사용):**
```bash
TOKEN=$(cat ~/.git-credentials | grep -o 'github_pat[^@]*' | head -1)
curl -s -X POST \
  -H "Authorization: token $TOKEN" \
  -H "Accept: application/vnd.github.v3+json" \
  https://api.github.com/repos/metamong-realty/metamong/pulls \
  -d "{\"title\": \"...\", \"head\": \"$BRANCH\", \"base\": \"$BASE\", \"body\": \"...\"}"
```

**Assignee 자동 설정:**
```bash
curl -s -X PATCH \
  -H "Authorization: token $TOKEN" \
  https://api.github.com/repos/metamong-realty/metamong/issues/{PR_NUMBER} \
  -d '{"assignees": ["mark1346"]}'
```

**Notion 티켓 상태 업데이트:**
```bash
curl -s -X PATCH https://api.notion.com/v1/pages/{PAGE_ID} \
  -H "Authorization: Bearer $NOTION_API_KEY" \
  -H "Notion-Version: 2022-06-28" \
  -H "Content-Type: application/json" \
  -d '{"properties": {"작업상태": {"select": {"name": "PR 생성"}}}}'
```

Notion에서 티켓 page_id는 티켓번호로 조회:
```bash
curl -s https://api.notion.com/v1/databases/328e9c4a8be480e3a024f8585197368c/query \
  -H "Authorization: Bearer $NOTION_API_KEY" \
  -H "Notion-Version: 2022-06-28" \
  -H "Content-Type: application/json" \
  -d "{\"filter\": {\"property\": \"티켓번호\", \"rich_text\": {\"equals\": \"$TICKET\"}}}"
```

### 5단계: 결과 보고

```
✅ PR 생성 완료!

브랜치: feat/META-009-oauth → main
PR: https://github.com/metamong-realty/metamong/pull/28
Notion: META-009 → PR 생성

커밋:
1. feat: [META-009] OAuth2 httpOnly cookie 기반 인증
2. style: ktlint auto-format
```

## 주의사항

- `.env`, `.env.local` 등 환경 파일은 커밋하지 않음
- 빌드/린트 실패 시 PR 생성 절대 금지
- UI 변경이 있으면 Puppeteer로 Before/After 스크린샷 찍어서 PR에 포함
- 스크린샷은 Telegram Bot API로 Mark에게도 전송
