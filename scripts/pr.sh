#!/bin/bash
# PR 생성 스크립트 — 반드시 이 스크립트를 통해 PR 생성
# 직접 curl/gh로 PR 만드는 것 금지
set -euo pipefail

ROOT=$(git rev-parse --show-toplevel)
cd "$ROOT"

TOKEN=$(cat ~/.git-credentials | grep -o 'github_pat[^@]*' | head -1)
BRANCH=$(git symbolic-ref --short HEAD)
TICKET=$(echo "$BRANCH" | grep -oE 'META-[0-9]+' | head -1 || true)

# ────────────────────────────────────────────
# 1단계: 변경사항 확인 + 사용자 승인
# ────────────────────────────────────────────
echo ""
echo "═══════════════════════════════════════════"
echo "  📋 PR 생성 체크리스트"
echo "═══════════════════════════════════════════"
echo ""
echo "브랜치: $BRANCH"
echo "티켓:   ${TICKET:-없음}"
echo ""
echo "변경 파일:"
git diff --name-only "$(git merge-base HEAD origin/main)" HEAD 2>/dev/null || git diff --name-only HEAD~1 HEAD
echo ""

# ────────────────────────────────────────────
# 2단계: base 브랜치 자동 판단
# ────────────────────────────────────────────
CHANGED=$(git diff --name-only "$(git merge-base HEAD origin/back-deploy 2>/dev/null || echo HEAD~1)" HEAD 2>/dev/null || git diff --name-only HEAD~1 HEAD)
FE_CHANGED=$(echo "$CHANGED" | grep '^front/' || true)
BE_CHANGED=$(echo "$CHANGED" | grep '^back/server\|^back/common' || true)
BATCH_CHANGED=$(echo "$CHANGED" | grep '^back/batch' || true)

if [ -n "$FE_CHANGED" ] && [ -n "$BE_CHANGED" ]; then
  echo "⚠️  FE + BE 변경이 혼재합니다. 반드시 분리하세요."
  echo "   FE → main / BE → back-deploy / Batch → back-batch-deploy"
  echo ""
  read -rp "계속 진행하시겠습니까? (분리 완료 확인) [y/N]: " CONFIRM
  [ "$CONFIRM" = "y" ] || { echo "중단합니다."; exit 1; }
fi

if [ -n "$BATCH_CHANGED" ]; then
  BASE="back-batch-deploy"
elif [ -n "$BE_CHANGED" ]; then
  BASE="back-deploy"
else
  BASE="main"
fi
echo "Base 브랜치: $BASE"

# ────────────────────────────────────────────
# 3단계: 품질 검사 (필수)
# ────────────────────────────────────────────
echo ""
echo "━━━ 🔍 품질 검사 시작 ━━━"

if [ -n "$BE_CHANGED" ] || [ -n "$BATCH_CHANGED" ]; then
  echo "📦 BE ktlintFormat..."
  ./gradlew :back:server:ktlintFormat --no-daemon -q 2>/dev/null || true
  echo "📦 BE build + test..."
  ./gradlew :back:server:ktlintCheck :back:server:build --no-daemon || {
    echo "❌ BE 빌드/테스트 실패 → PR 생성 중단"
    exit 1
  }
  echo "✅ BE 통과"
fi

if [ -n "$FE_CHANGED" ]; then
  echo "📦 FE build..."
  cd front && npm run build || {
    echo "❌ FE 빌드 실패 → PR 생성 중단"
    exit 1
  }
  cd "$ROOT"
  echo "✅ FE 통과"
fi

# ktlintFormat으로 변경된 파일 있으면 자동 커밋
if ! git diff --quiet; then
  echo "🎨 ktlint auto-format 변경 감지 → 자동 커밋"
  git add -A
  git commit -m "style: ktlint auto-format"
  git push origin "$BRANCH"
fi

# ────────────────────────────────────────────
# 4단계: UI 변경 시 스크린샷
# ────────────────────────────────────────────
if [ -n "$FE_CHANGED" ]; then
  echo ""
  read -rp "UI 변경이 있나요? Before/After 스크린샷을 찍을까요? [y/N]: " SCREENSHOT
  if [ "$SCREENSHOT" = "y" ]; then
    echo "📸 FE 서버 실행 후 스크린샷 찍습니다..."
    # FE dev 서버 기동 및 Puppeteer 스크린샷 (별도 구현)
    echo "⚠️  스크린샷 자동화는 별도 실행 필요: node scripts/screenshot.js"
  fi
fi

# ────────────────────────────────────────────
# 5단계: PR 정보 입력
# ────────────────────────────────────────────
echo ""
echo "━━━ 📝 PR 정보 입력 ━━━"
echo ""

DEFAULT_TITLE=""
if [ -n "$TICKET" ]; then
  TYPE=$(echo "$BRANCH" | cut -d'/' -f1)
  DEFAULT_TITLE="${TYPE}: [${TICKET}] "
fi

read -rp "PR 제목 [${DEFAULT_TITLE}]: " TITLE
TITLE="${TITLE:-$DEFAULT_TITLE}"

echo ""
echo "PR 본문을 입력하세요 (엔터 2번으로 종료):"
echo "  - 개요: 배경 + 문제 + 해결"
echo "  - 변경사항: 비즈니스 관점으로"
echo "  - 테스트 결과"
echo ""
BODY=""
PREV_LINE=""
while IFS= read -r LINE; do
  if [ -z "$LINE" ] && [ -z "$PREV_LINE" ]; then
    break
  fi
  BODY="${BODY}${LINE}\n"
  PREV_LINE="$LINE"
done

# ────────────────────────────────────────────
# 6단계: PR 생성
# ────────────────────────────────────────────
echo ""
echo "━━━ 🚀 PR 생성 ━━━"

# push 먼저
git push origin "$BRANCH"

BODY_JSON=$(printf '%s' "$BODY" | python3 -c "import sys,json; print(json.dumps(sys.stdin.read()))")

PR_NUM=$(curl -s -X POST \
  -H "Authorization: token $TOKEN" \
  -H "Accept: application/vnd.github.v3+json" \
  https://api.github.com/repos/metamong-realty/metamong/pulls \
  -d "{\"title\": $(echo "$TITLE" | python3 -c 'import sys,json; print(json.dumps(sys.stdin.read().strip()))'), \"head\": \"$BRANCH\", \"base\": \"$BASE\", \"body\": $BODY_JSON}" \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('number', 'ERR'))")

if [ "$PR_NUM" = "ERR" ]; then
  echo "❌ PR 생성 실패"
  exit 1
fi

# Assignee 설정
curl -s -X PATCH \
  -H "Authorization: token $TOKEN" \
  -H "Accept: application/vnd.github.v3+json" \
  "https://api.github.com/repos/metamong-realty/metamong/issues/$PR_NUM" \
  -d '{"assignees":["mark1346"]}' > /dev/null

echo "✅ PR #$PR_NUM 생성: https://github.com/metamong-realty/metamong/pull/$PR_NUM"

# ────────────────────────────────────────────
# 7단계: Notion 업데이트
# ────────────────────────────────────────────
if [ -n "$TICKET" ] && [ -n "${NOTION_API_KEY:-}" ]; then
  PAGE_ID=$(curl -s "https://api.notion.com/v1/databases/328e9c4a8be480e3a024f8585197368c/query" \
    -H "Authorization: Bearer $NOTION_API_KEY" \
    -H "Notion-Version: 2022-06-28" \
    -H "Content-Type: application/json" \
    -d "{\"filter\": {\"property\": \"티켓번호\", \"rich_text\": {\"equals\": \"$TICKET\"}}}" \
    | python3 -c "import sys,json; r=json.load(sys.stdin)['results']; print(r[0]['id'] if r else '')" 2>/dev/null || true)

  if [ -n "$PAGE_ID" ]; then
    curl -s -X PATCH "https://api.notion.com/v1/pages/$PAGE_ID" \
      -H "Authorization: Bearer $NOTION_API_KEY" \
      -H "Notion-Version: 2022-06-28" \
      -H "Content-Type: application/json" \
      -d '{"properties": {"작업상태": {"select": {"name": "PR 생성"}}}}' > /dev/null
    echo "✅ Notion $TICKET → PR 생성"
  fi
fi

echo ""
echo "═══════════════════════════════════════════"
echo "  완료!"
echo "  PR: https://github.com/metamong-realty/metamong/pull/$PR_NUM"
echo "═══════════════════════════════════════════"
