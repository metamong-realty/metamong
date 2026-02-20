---
description: "변경된 코드를 Gemini API로 전송하여 외부 관점의 코드 리뷰를 받습니다"
---

변경된 코드를 Gemini API에 전송하여 외부 AI 모델의 독립적 시각으로 코드 리뷰를 수행하라.

## 실행 단계

### 1. 환경 검증

Bash로 `GEMINI_API_KEY` 환경변수 존재 여부를 확인하라:

```bash
echo "${GEMINI_API_KEY:+SET}"
```

결과가 `SET`이 아니면 아래 안내를 출력하고 **즉시 종료**하라:

```
GEMINI_API_KEY 환경변수가 설정되지 않았습니다.

설정 방법:
  export GEMINI_API_KEY="your-api-key"

API 키 발급: https://aistudio.google.com/apikey
```

### 2. 변경사항 수집

인자에 따라 적절한 git diff 명령을 선택하라:

| 인자 | 명령 |
|------|------|
| (기본값) | `git diff HEAD` |
| `--branch <name>` | `git diff <name>...HEAD` |
| `--staged` | `git diff --cached` |

diff 결과가 비어있으면 아래를 출력하고 종료하라:

```
변경된 코드가 없습니다. 커밋되지 않은 변경사항이 있는지 확인해주세요.
```

### 3. 헬퍼 스크립트 실행

수집한 diff를 stdin으로 파이프하여 헬퍼 스크립트를 실행하라:

```bash
git diff HEAD | bash .claude/skills/gemini-review/gemini-review.sh
```

브랜치 비교나 staged 옵션인 경우 해당 diff 명령으로 대체하라.

### 4. 결과 표시

스크립트 출력을 사용자에게 그대로 전달하라. 성공 시 아래 안내를 추가하라:

```
---
다음 단계:
- 필수 수정 사항이 있다면 즉시 반영하세요
- `/code-review`로 Claude 관점의 내부 리뷰도 함께 확인할 수 있습니다
```

스크립트가 실패하면 에러 메시지를 그대로 전달하라.
