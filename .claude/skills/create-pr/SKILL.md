---
description: "커밋을 논리 단위로 분리하고 PR을 자동 생성합니다. 사용자가 'PR', 'PR 만들어줘', 'PR 올려줘'라는 단어를 언급하면 생성 요청인지 확인 후 실행."
allowed-tools: Bash, Read, Write, Edit
---

# create-pr — PR 자동 생성

**반드시 `scripts/pr.sh`를 실행한다. 직접 curl/gh로 PR 만들지 않는다.**

```bash
cd ~/projects/metamong
bash scripts/pr.sh
```

스크립트가 다음을 자동으로 처리한다:
1. 변경사항 출력 + 사용자 확인
2. FE/BE 혼재 경고
3. base 브랜치 자동 판단 (FE→main, BE→back-deploy, Batch→back-batch-deploy)
4. ktlintFormat + build + test (실패 시 PR 생성 중단)
5. UI 변경 시 스크린샷 여부 확인
6. PR 제목/본문 입력 (비즈니스 관점)
7. GitHub PR 생성 + Assignee mark1346 자동 설정
8. Notion 티켓 상태 → `PR 생성` 자동 업데이트

## 스크립트를 실행할 수 없는 경우

interactive 환경이 아닐 때만 수동 처리 가능하되, 다음 체크리스트를 반드시 순서대로 이행:

1. `git diff --name-only`로 변경 파일 확인 후 Mark에게 보여주고 승인 받기
2. FE/BE 분리 확인
3. `./gradlew :back:server:ktlintFormat && ./gradlew :back:server:build` (BE 변경 시)
4. `cd front && npm run build` (FE 변경 시)
5. UI 변경 시 Puppeteer 스크린샷 찍어서 PR description에 포함 + Telegram 전송
6. GitHub API로 PR 생성 (base 브랜치 정확히)
7. Assignee mark1346 설정
8. Notion 티켓 `PR 생성` 업데이트
