#!/bin/bash
# PostToolUse hook: Skill/Task 도구 호출 시 사용 이력 로깅
# - Skill: 어떤 스킬이 호출되었는지 + 인자
# - Task: 어떤 서브에이전트가 생성되었는지 + 프롬프트 요약
set -uo pipefail

LOG_DIR="$CLAUDE_PROJECT_DIR/logs"
LOG_FILE="$LOG_DIR/skill-agent-usage.log"
mkdir -p "$LOG_DIR"

INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty')
TIMESTAMP=$(date +"%Y-%m-%d %H:%M:%S")
SESSION_ID=$(echo "$INPUT" | jq -r '.session_id // "unknown"' | tail -c 9)

if [ "$TOOL_NAME" = "Skill" ]; then
  SKILL=$(echo "$INPUT" | jq -r '.tool_input.skill // empty')
  ARGS=$(echo "$INPUT" | jq -r '.tool_input.args // ""' | head -c 200)
  {
    printf "%s | SKILL  | %-20s | %s\n" "$TIMESTAMP" "$SKILL" "$SESSION_ID"
    if [ -n "$ARGS" ]; then
      printf "  args: %s\n" "$ARGS"
    fi
    echo "---"
  } >> "$LOG_FILE"

elif [ "$TOOL_NAME" = "Task" ]; then
  AGENT=$(echo "$INPUT" | jq -r '.tool_input.subagent_type // empty')
  DESC=$(echo "$INPUT" | jq -r '.tool_input.description // ""')
  PROMPT=$(echo "$INPUT" | jq -r '.tool_input.prompt // ""' | head -c 300)
  {
    printf "%s | AGENT  | %-20s | %s\n" "$TIMESTAMP" "$AGENT" "$SESSION_ID"
    printf "  desc: %s\n" "$DESC"
    printf "  prompt: %s\n" "$PROMPT"
    echo "---"
  } >> "$LOG_FILE"
fi

exit 0
