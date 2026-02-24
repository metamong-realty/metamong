#!/usr/bin/env bash
set -euo pipefail

# ─── 설정 ───────────────────────────────────────────────
MAX_FILES=20
MAX_FILE_BYTES=204800  # 200KB per file
MAX_TOTAL_BYTES=512000 # 500KB total source context
GEMINI_MODEL="gemini-3-flash-preview"
GEMINI_URL="https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent"

# ─── 환경 검증 ──────────────────────────────────────────
command -v gcloud >/dev/null 2>&1 || { echo "ERROR: gcloud CLI가 설치되어 있지 않습니다. https://cloud.google.com/sdk/docs/install" >&2; exit 1; }

ACCESS_TOKEN=$(gcloud auth print-access-token 2>/dev/null)
if [[ -z "$ACCESS_TOKEN" ]]; then
  echo "ERROR: gcloud 인증이 필요합니다. 'gcloud auth login'을 실행해주세요." >&2
  exit 1
fi

command -v jq >/dev/null 2>&1 || { echo "ERROR: jq가 설치되어 있지 않습니다. brew install jq" >&2; exit 1; }
command -v curl >/dev/null 2>&1 || { echo "ERROR: curl이 설치되어 있지 않습니다." >&2; exit 1; }

# ─── stdin에서 diff 읽기 ────────────────────────────────
DIFF=$(cat)

if [[ -z "$DIFF" ]]; then
  echo "변경된 코드가 없습니다."
  exit 0
fi

# ─── 민감 파일 필터링 ───────────────────────────────────
DIFF=$(echo "$DIFF" | awk '
  /^diff --git/ {
    skip = 0
    if ($0 ~ /\.env/ || $0 ~ /-prod\.yml/ || $0 ~ /credentials/ || $0 ~ /secret/) {
      skip = 1
    }
  }
  !skip { print }
')

if [[ -z "$DIFF" ]]; then
  echo "변경사항이 모두 민감 파일(.env, *-prod.yml 등)이므로 리뷰를 건너뜁니다."
  exit 0
fi

# ─── 프로젝트 루트 탐색 ──────────────────────────────────
PROJECT_ROOT=$(git rev-parse --show-toplevel 2>/dev/null)
if [[ -z "$PROJECT_ROOT" ]]; then
  echo "ERROR: git 저장소를 찾을 수 없습니다." >&2
  exit 1
fi

# ─── 변경 파일 전체 소스 수집 ─────────────────────────────
SOURCE_EXTENSIONS='\.kt$|\.java$|\.yml$|\.yaml$|\.properties$|\.gradle\.kts$|\.xml$|\.json$|\.ts$|\.vue$|\.js$|\.md$|\.sh$'

# diff에서 변경 파일 목록 추출 (Added, Copied, Modified, Renamed만)
CHANGED_FILES=$(echo "$DIFF" | grep -E '^diff --git' | sed 's|diff --git a/.* b/||' | sort -u)

FILE_CONTEXT=""
CONTEXT_MODE="full"
FILE_COUNT=0
TOTAL_BYTES=0

for filepath in $CHANGED_FILES; do
  # 소스 파일만 필터링
  echo "$filepath" | grep -qE "$SOURCE_EXTENSIONS" || continue
  # 빌드/생성 파일 제외
  echo "$filepath" | grep -qE '(build/|dist/|node_modules/|\.class$|\.jar$)' && continue
  # 파일 존재 확인 (삭제된 파일 제외)
  [[ -f "$PROJECT_ROOT/$filepath" ]] || continue

  FILE_SIZE=$(wc -c < "$PROJECT_ROOT/$filepath")

  # 파일당 크기 제한
  if (( FILE_SIZE > MAX_FILE_BYTES )); then
    continue
  fi

  # 총량 제한 체크
  if (( FILE_COUNT >= MAX_FILES )) || (( TOTAL_BYTES + FILE_SIZE > MAX_TOTAL_BYTES )); then
    CONTEXT_MODE="fallback"
    break
  fi

  FILE_CONTEXT+="### ${filepath}
\`\`\`
$(cat "$PROJECT_ROOT/$filepath")
\`\`\`

"
  FILE_COUNT=$((FILE_COUNT + 1))
  TOTAL_BYTES=$((TOTAL_BYTES + FILE_SIZE))
done

# fallback: 전체 소스 대신 확장 컨텍스트 diff 사용
if [[ "$CONTEXT_MODE" == "fallback" ]]; then
  FILE_CONTEXT=""
  echo "INFO: 변경 파일이 많아(${FILE_COUNT}+개) 확장 컨텍스트 모드로 리뷰합니다." >&2
fi

# ─── 프로젝트 컨텍스트 동적 로드 ────────────────────────
CONTEXT=""

# CLAUDE.md 로드
if [[ -f "$PROJECT_ROOT/CLAUDE.md" ]]; then
  CONTEXT+="## CLAUDE.md (프로젝트 개요)
$(cat "$PROJECT_ROOT/CLAUDE.md")

"
fi

# .claude/rules/*.md 로드
if [[ -d "$PROJECT_ROOT/.claude/rules" ]]; then
  for rule_file in "$PROJECT_ROOT"/.claude/rules/*.md; do
    [[ -f "$rule_file" ]] || continue
    rule_name=$(basename "$rule_file" .md)
    CONTEXT+="## Rule: ${rule_name}
$(cat "$rule_file")

"
  done
fi

if [[ -z "$CONTEXT" ]]; then
  echo "WARN: 프로젝트 컨텍스트 파일을 찾을 수 없습니다. 기본 리뷰를 진행합니다." >&2
fi

# ─── 프롬프트 구성 ──────────────────────────────────────
# 파일 컨텍스트 섹션 구성
if [[ -n "$FILE_CONTEXT" ]]; then
  SOURCE_SECTION="# 변경된 파일 전체 소스 (${FILE_COUNT}개 파일)

아래는 변경된 파일들의 전체 소스코드입니다. diff와 함께 참고하여 전체 맥락을 파악하세요.

${FILE_CONTEXT}"
else
  SOURCE_SECTION="(변경 파일이 많아 전체 소스를 포함하지 못했습니다. diff만으로 리뷰하세요.)"
fi

PROMPT="당신은 코드 리뷰 전문가입니다.

아래 프로젝트 컨벤션 문서를 숙지한 뒤, 변경된 코드를 리뷰하세요.

# 프로젝트 컨벤션

${CONTEXT}
${SOURCE_SECTION}

# 변경된 코드 (diff)

\`\`\`diff
${DIFF}
\`\`\`

# 리뷰 지시

위 컨벤션 문서를 기준으로 변경사항을 아래 5가지 관점에서 리뷰하세요:

1. **코드 품질** — 프로젝트 컨벤션 준수, 아키텍처 의존성 방향, SOLID 원칙
2. **보안** — 입력 검증, SQL Injection, 민감정보 노출, 인증/인가
3. **성능** — N+1 쿼리, 불필요한 객체 생성, 인덱스, 캐시
4. **테스트** — 테스트 존재 여부, 커버리지, 엣지 케이스
5. **문서화** — Swagger 어노테이션, 복잡한 로직 주석

# 응답 형식

아래 마크다운 형식으로 응답하세요:

## Gemini 코드 리뷰 결과

### 잘된 점
- 구체적인 칭찬 포인트

### 개선 필요
- \`파일명:라인번호\` — 개선사항 설명 및 이유

### 필수 수정
- 보안/성능 관련 반드시 수정해야 할 이슈 (없으면 \"없음\")

### 제안사항
- 코드 품질 향상을 위한 추가 제안

**객관적으로 분석하세요. 무조건 칭찬하지 마세요. 리뷰나 제안 시 반드시 기술적 근거를 함께 제시하세요.**"

# ─── JSON 페이로드 생성 ─────────────────────────────────
TMPFILE=$(mktemp)
trap 'rm -f "$TMPFILE"' EXIT

jq -n --arg text "$PROMPT" '{
  contents: [{
    parts: [{
      text: $text
    }]
  }],
  generationConfig: {
    temperature: 0.3,
    maxOutputTokens: 8192
  }
}' > "$TMPFILE"

# ─── API 호출 ───────────────────────────────────────────
HTTP_CODE=$(curl -s -o /dev/stdout -w "\n%{http_code}" \
  -X POST "$GEMINI_URL" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d @"$TMPFILE" 2>/dev/null)

# 마지막 줄에서 HTTP 코드 추출
RESPONSE_BODY=$(echo "$HTTP_CODE" | sed '$d')
HTTP_STATUS=$(echo "$HTTP_CODE" | tail -1)

# ─── 응답 처리 ──────────────────────────────────────────
case "$HTTP_STATUS" in
  200)
    REVIEW=$(echo "$RESPONSE_BODY" | jq -r '.candidates[0].content.parts[0].text // empty')
    if [[ -z "$REVIEW" ]]; then
      echo "ERROR: Gemini 응답에서 리뷰 텍스트를 추출할 수 없습니다." >&2
      echo "Raw response:" >&2
      echo "$RESPONSE_BODY" | jq '.' >&2 2>/dev/null || echo "$RESPONSE_BODY" >&2
      exit 1
    fi
    echo "$REVIEW"
    ;;
  400)
    echo "ERROR: 잘못된 요청입니다. diff 크기가 너무 클 수 있습니다." >&2
    echo "$RESPONSE_BODY" | jq -r '.error.message // empty' >&2 2>/dev/null
    exit 1
    ;;
  401|403)
    echo "ERROR: 인증이 만료되었거나 권한이 없습니다. 'gcloud auth login'으로 재인증해주세요." >&2
    exit 1
    ;;
  429)
    echo "ERROR: Rate Limit 초과. 잠시 후 다시 시도해주세요." >&2
    exit 1
    ;;
  5*)
    echo "ERROR: Gemini API 서버 오류 (HTTP $HTTP_STATUS). 잠시 후 다시 시도해주세요." >&2
    exit 1
    ;;
  *)
    echo "ERROR: 예상치 못한 응답 (HTTP $HTTP_STATUS)" >&2
    echo "$RESPONSE_BODY" >&2
    exit 1
    ;;
esac
