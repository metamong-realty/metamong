#!/bin/bash
# PreToolUse hook: Repository 파일 Write|Edit 시 같은 도메인의 기존 메서드 목록을 표시
# 목적: 중복 쿼리 메서드 생성 방지 (판단은 LLM에게 위임)
set -euo pipefail

INPUT=$(cat)
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')

if [ -z "$FILE_PATH" ]; then
  exit 0
fi

# Repository 관련 .kt 파일만 대상
if [[ "$FILE_PATH" != *Repository*.kt ]]; then
  exit 0
fi

REPO_DIR=$(dirname "$FILE_PATH")
CURRENT_FILE=$(basename "$FILE_PATH")

# 같은 디렉토리의 모든 Repository 파일에서 메서드 시그니처 수집
METHODS=""
for repo_file in "$REPO_DIR"/*Repository*.kt; do
  [ -f "$repo_file" ] || continue
  basename_file=$(basename "$repo_file")
  # fun 키워드로 시작하는 메서드 시그니처 추출 (interface + class 모두)
  found=$(grep -nE '^\s*(override\s+)?fun\s+\w+' "$repo_file" 2>/dev/null | head -30 || true)
  if [ -n "$found" ]; then
    METHODS="${METHODS}
--- ${basename_file} ---
${found}
"
  fi
done

# JPA Repository의 method naming 쿼리도 포함 (findBy, existsBy 등)
for repo_file in "$REPO_DIR"/*Repository*.kt; do
  [ -f "$repo_file" ] || continue
  basename_file=$(basename "$repo_file")
  jpa_methods=$(grep -nE '^\s*fun\s+(findBy|findAllBy|existsBy|countBy|deleteBy)' "$repo_file" 2>/dev/null | head -20 || true)
  if [ -n "$jpa_methods" ] && [[ "$METHODS" != *"$jpa_methods"* ]]; then
    METHODS="${METHODS}
--- ${basename_file} (JPA method naming) ---
${jpa_methods}
"
  fi
done

if [ -z "$METHODS" ]; then
  exit 0
fi

# 정보성 메시지 출력 (BLOCK하지 않음 - exit 0)
echo "=== Repository 중복 쿼리 방지 체크 ==="
echo "현재 편집: ${CURRENT_FILE}"
echo ""
echo "[같은 도메인의 기존 Repository 메서드 목록]"
echo "$METHODS"
echo ""
echo "위 목록에서 재사용 가능한 메서드가 있는지 확인하세요."
echo "기존 메서드의 시그니처/반환타입 변경은 지양합니다."
echo "==================================="

exit 0
