# Git Workflow Guidelines

## 브랜치 전략

### GitFlow 기반 브랜치 모델
```
main (production)
 └── develop (development)
     ├── feature/[TICKET-123]-user-authentication
     ├── feature/[TICKET-124]-post-crud
     └── release/v1.2.0
hotfix/[URGENT-001]-security-patch
```

### 브랜치 명명 규칙
```bash
# Feature 브랜치
feature/[TICKET-NUMBER]-brief-description
feature/META-123-user-authentication
feature/META-124-post-crud-api

# Hotfix 브랜치  
hotfix/[URGENT-NUMBER]-brief-description
hotfix/URGENT-001-security-vulnerability

# Release 브랜치
release/v[VERSION]
release/v1.2.0

# Bugfix 브랜치
bugfix/[TICKET-NUMBER]-brief-description
bugfix/META-125-login-error
```

## 커밋 메시지 규칙

### 커밋 메시지 포맷
```
[TYPE]: [TICKET-NUMBER] 제목

본문 (선택사항)

하단 메타 정보 (선택사항)
```

### TYPE 분류
```bash
feat:     # 새로운 기능 추가
fix:      # 버그 수정
docs:     # 문서 변경
style:    # 코드 포맷팅, 세미콜론 누락 등
refactor: # 코드 리팩토링
test:     # 테스트 코드 추가/수정
chore:    # 빌드 프로세스, 보조 도구 변경
perf:     # 성능 개선
ci:       # CI/CD 설정 변경
build:    # 빌드 관련 파일 수정
```

### 커밋 메시지 예시
```bash
# Good Examples
feat: [META-123] 사용자 회원가입 API 구현

fix: [META-124] 로그인 시 토큰 만료 처리 오류 수정

refactor: [META-125] UserService 코드 정리 및 가독성 개선

test: [META-126] UserController 단위 테스트 추가

docs: [META-127] API 문서 업데이트

# Bad Examples  
feat: add user feature
fix: bug
update: some files
```

### 상세 커밋 메시지
```bash
feat: [META-123] 사용자 회원가입 API 구현

- 이메일 중복 검증 로직 추가
- BCrypt를 이용한 패스워드 암호화
- JWT 토큰 발급 기능
- 회원가입 완료 이메일 발송

Resolves: META-123
Reviewed-by: @김개발자
```

## Pull Request 규칙

### PR 제목 형식
```
[TICKET-NUMBER] PR 제목
[META-123] 사용자 인증 기능 구현
```

### PR 템플릿
```markdown
## 🚀 변경 사항
- [ ] 새로운 기능 추가
- [ ] 버그 수정
- [ ] 문서 업데이트
- [ ] 리팩토링
- [ ] 테스트 추가

## 📋 상세 설명
### 구현 내용
- 사용자 회원가입 API 구현
- JWT 기반 인증 시스템
- 이메일 중복 체크 로직

### 변경된 파일
- `UserController.kt` - 회원가입 엔드포인트 추가
- `UserService.kt` - 비즈니스 로직 구현
- `UserRepository.kt` - 데이터 접근 계층

## 🧪 테스트
- [ ] 단위 테스트 작성 완료
- [ ] 통합 테스트 작성 완료
- [ ] 수동 테스트 완료

### 테스트 시나리오
1. 정상적인 회원가입 시나리오
2. 이메일 중복시 오류 처리
3. 유효하지 않은 입력값 처리

## 📊 성능 영향
- 예상 응답 시간: < 200ms
- 메모리 사용량: 변화 없음
- DB 쿼리 수: +2개 (중복 체크, 사용자 저장)

## 🔗 관련 이슈
- Closes #META-123
- Related to #META-124

## 📸 스크린샷 (필요시)
[API 테스트 결과 이미지]

## 🔍 리뷰 요청 사항
- [ ] 보안 검토 필요
- [ ] 성능 검토 필요
- [ ] 아키텍처 검토 필요

## ✅ 체크리스트
- [ ] ktlint 포맷팅 완료
- [ ] 테스트 통과 확인
- [ ] 문서 업데이트 완료
- [ ] 보안 체크 완료
```

### PR 리뷰 규칙
```bash
# 승인 기준
- 최소 2명의 리뷰어 승인
- 모든 자동화 테스트 통과
- 코드 커버리지 80% 이상 유지
- 보안 체크 통과

# 리뷰어 지정
- 기능 담당자: 필수 리뷰어
- 시니어 개발자: 아키텍처 리뷰
- 보안 담당자: 보안 관련 변경시
```

## 릴리즈 프로세스

### 버전 관리 (Semantic Versioning)
```bash
v[MAJOR].[MINOR].[PATCH]

v1.0.0  # 첫 번째 릴리즈
v1.0.1  # 버그 수정
v1.1.0  # 새로운 기능 추가
v2.0.0  # Breaking Change
```

### 릴리즈 브랜치
```bash
# 릴리즈 브랜치 생성
git checkout develop
git pull origin develop
git checkout -b release/v1.2.0

# 버전 정보 업데이트
# - build.gradle.kts의 version 수정
# - CHANGELOG.md 업데이트

# 릴리즈 브랜치에서 테스트 및 버그 수정

# 릴리즈 완료 시 main과 develop에 병합
git checkout main
git merge release/v1.2.0
git tag v1.2.0

git checkout develop
git merge release/v1.2.0

# 릴리즈 브랜치 삭제
git branch -d release/v1.2.0
```

### CHANGELOG.md 형식
```markdown
# Changelog

## [1.2.0] - 2024-02-05

### Added
- [META-123] 사용자 회원가입 기능
- [META-124] JWT 기반 인증 시스템
- [META-125] 이메일 인증 기능

### Changed
- [META-126] 패스워드 암호화 알고리즘 개선
- [META-127] API 응답 형식 통일

### Fixed
- [META-128] 로그인 실패 시 오류 메시지 수정
- [META-129] 토큰 만료 시간 검증 오류 수정

### Security
- [META-130] SQL Injection 취약점 수정
- [META-131] XSS 방지 필터 추가

## [1.1.0] - 2024-01-15
...
```

## Git Hooks 설정

### Pre-commit Hook
```bash
#!/bin/sh
# .git/hooks/pre-commit

echo "Running pre-commit checks..."

# ktlint 검사
./gradlew ktlintCheck
if [ $? -ne 0 ]; then
  echo "ktlint check failed. Run './gradlew ktlintFormat' to fix."
  exit 1
fi

# 테스트 실행
./gradlew test
if [ $? -ne 0 ]; then
  echo "Tests failed. Please fix failing tests."
  exit 1
fi

# 보안 체크 (예시)
grep -r "password.*=" src/ && echo "Hard-coded password detected!" && exit 1
grep -r "secret.*=" src/ && echo "Hard-coded secret detected!" && exit 1

echo "Pre-commit checks passed!"
```

### Commit Message Hook
```bash
#!/bin/sh
# .git/hooks/commit-msg

commit_regex='^(feat|fix|docs|style|refactor|test|chore|perf|ci|build): \[META-[0-9]+\] .+'

if ! grep -qE "$commit_regex" "$1"; then
    echo "Invalid commit message format!"
    echo "Format: [TYPE]: [META-XXX] description"
    echo "Example: feat: [META-123] 사용자 로그인 기능 추가"
    exit 1
fi
```

## 협업 워크플로

### Daily 워크플로
```bash
# 1. 아침에 최신 코드 동기화
git checkout develop
git pull origin develop

# 2. 새 기능 브랜치 생성
git checkout -b feature/META-123-user-auth

# 3. 개발 진행
# ... 코딩 ...

# 4. 수시로 커밋
git add .
git commit -m "feat: [META-123] 사용자 인증 API 기본 구조 구현"

# 5. 중간에 develop 변경사항 반영
git checkout develop
git pull origin develop
git checkout feature/META-123-user-auth
git rebase develop  # 또는 git merge develop

# 6. 기능 완료 후 PR 생성
git push origin feature/META-123-user-auth
# GitHub에서 PR 생성

# 7. 리뷰 완료 후 병합
# Squash and merge 권장

# 8. 로컬 정리
git checkout develop
git pull origin develop
git branch -d feature/META-123-user-auth
```

### Hotfix 워크플로
```bash
# 1. main에서 hotfix 브랜치 생성
git checkout main
git pull origin main
git checkout -b hotfix/URGENT-001-security-patch

# 2. 긴급 수정
# ... 수정 작업 ...

# 3. 테스트 및 커밋
git commit -m "fix: [URGENT-001] 보안 취약점 수정"

# 4. main과 develop에 병합
git checkout main
git merge hotfix/URGENT-001-security-patch
git tag v1.1.1

git checkout develop
git merge hotfix/URGENT-001-security-patch

# 5. 즉시 배포
# CI/CD 파이프라인 트리거

# 6. 정리
git branch -d hotfix/URGENT-001-security-patch
```

## 코드 리뷰 가이드라인

### 리뷰어 체크리스트
```markdown
## 기능 측면
- [ ] 요구사항이 정확히 구현되었는가?
- [ ] 예외 상황이 적절히 처리되는가?
- [ ] 테스트 코드가 충분한가?

## 코드 품질
- [ ] 코드가 읽기 쉽고 이해하기 쉬운가?
- [ ] 네이밍이 명확한가?
- [ ] 주석이 필요한 부분에 적절히 작성되었는가?
- [ ] SOLID 원칙을 준수하는가?

## 성능
- [ ] 성능 이슈가 없는가? (N+1, 대용량 데이터 처리 등)
- [ ] 적절한 캐싱이 적용되었는가?

## 보안
- [ ] 보안 취약점이 없는가?
- [ ] 입력값 검증이 충분한가?
- [ ] 인증/인가가 적절히 처리되는가?

## 기술적 부채
- [ ] 중복 코드가 없는가?
- [ ] 하드코딩된 값이 없는가?
- [ ] 의존성이 적절히 관리되는가?
```

### 피드백 작성 규칙
```markdown
# 좋은 피드백 예시
## 💡 제안
이 부분은 Stream을 사용하면 더 간결하게 작성할 수 있을 것 같습니다.
[코드 예시 제공]

## ❗ 이슈
N+1 문제가 발생할 수 있습니다. 
배치 로딩이나 JOIN을 고려해보세요.

## 👍 칭찬
예외 처리가 매우 잘 되어 있네요!

# 나쁜 피드백 예시
- "이건 틀렸습니다" (이유 없음)
- "다시 해주세요" (구체적 지침 없음)
- "성능이 안 좋을 것 같은데요" (근거 없음)
```

## Git 유용한 명령어

### 일상적 명령어
```bash
# 커밋 수정
git commit --amend -m "새로운 커밋 메시지"

# 최근 n개 커밋 합치기
git rebase -i HEAD~3

# 특정 파일만 스테이지
git add -p filename

# 임시 저장
git stash
git stash pop

# 브랜치 간 특정 커밋만 가져오기
git cherry-pick <commit-hash>

# 파일 히스토리 확인
git log --follow --patch -- filename

# 코드 작성자 확인
git blame filename
```

### 트러블슈팅
```bash
# 실수로 커밋한 파일 제거 (이미 푸시 전)
git rm --cached filename
git commit --amend

# 푸시한 커밋 되돌리기
git revert <commit-hash>

# 강제 푸시 (주의!)
git push --force-with-lease

# 파일 복구
git checkout HEAD -- filename
```