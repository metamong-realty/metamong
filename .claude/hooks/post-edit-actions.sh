#!/bin/bash
# PostToolUse hook: Write|Edit 후 파일 패턴에 따라 자동 액션 실행
# - .kt 파일: ktlintFormat
set -euo pipefail

INPUT=$(cat)
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')

if [ -z "$FILE_PATH" ]; then
  exit 0
fi

cd "$CLAUDE_PROJECT_DIR"

# Kotlin 파일 편집 시 변경 파일만 포맷팅
if [[ "$FILE_PATH" == *.kt ]]; then
  RELATIVE_PATH="${FILE_PATH#$CLAUDE_PROJECT_DIR/}"
  ./gradlew ktlintFormat -PktlintFiles="$RELATIVE_PATH" --quiet 2>/dev/null || \
    ./gradlew ktlintFormat --quiet 2>/dev/null || true
  exit 0
fi

exit 0
