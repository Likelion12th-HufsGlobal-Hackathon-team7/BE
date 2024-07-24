우리의 Git 컨벤션은 다음과 같습니다

# Branch 컨벤션
- master: 배포 가능한 상태만을 관리하는 브랜치
- hotfix: 출시 버전에서 발생한 버그를 수정 하는 브랜치
- release: 이번 출시 버전을 준비하는 브랜치
---
- dev: 다음 출시 버전을 개발하는 브랜치
- feat: 기능을 개발하는 브랜치

# Commit 컨벤션
- feat: 새로운 기능 추가
- fix: 버그 수정
- docs: 문서 수정
- refactor: 코드 리팩토링
- chore: 빌드 업무 수정, 패키지 매니저 수정
- add : 기타 분류하기 애매한 추가사항

# Branch 네이밍
- 유형/issue-#번호
- ex) feat/issue-#1

# Commit 메시지 네이밍
- [유형] issue-#번호 : 메시지
- ex) [feat] issue-#1 : 로그인 기능 구현
- [유형] issue-#번호,#번호 : 메시지 (여러 이슈번호가 있을 경우)
- ex) [feat] issue-#1,#2 : 로그인 기능 구현