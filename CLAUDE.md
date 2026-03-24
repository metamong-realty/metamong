# CLAUDE.md — Claude Code 행동 규칙

이 파일은 Claude Code가 반드시 따라야 하는 프로젝트 규칙이다.

## 🚨 PR 생성 규칙 (MANDATORY)

**PR은 반드시 `scripts/pr.sh`를 통해서만 생성한다.**

```bash
bash scripts/pr.sh
```

다음은 절대 금지:
- `curl`로 직접 GitHub API 호출해서 PR 생성
- `gh pr create` 직접 호출
- 빌드/테스트 통과 없이 PR 생성

이유: 빌드 검증, FE/BE 분리 확인, Notion 업데이트, 스크린샷을 보장하기 위함.

## 📦 배포 브랜치

| 변경 대상 | PR base 브랜치 |
|----------|--------------|
| FE (`front/`) | `main` |
| BE server (`back/server/`) | `back-deploy` |
| BE batch (`back/batch/`) | `back-batch-deploy` |

FE + BE 혼재 시 반드시 분리.

## ✅ PR 전 체크리스트

- [ ] `scripts/pr.sh` 실행했는가?
- [ ] 빌드/테스트 통과했는가?
- [ ] FE와 BE가 올바르게 분리됐는가?
- [ ] UI 변경 시 Before/After 스크린샷 포함했는가?
- [ ] Assignee `mark1346` 설정됐는가?
- [ ] Notion 티켓 상태가 `PR 생성`으로 업데이트됐는가?

## 브랜치명 규칙

```
{type}/META-{번호}-{설명}
예: fix/META-003-fix-trade-count
```

## PR 타이틀 규칙

```
{type}: [META-{번호}] 설명
예: fix: [META-003] 거래 건수 0으로 표시되는 문제 수정
```
