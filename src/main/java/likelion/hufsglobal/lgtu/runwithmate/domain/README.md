여기에는 도메인과 관련된 클래스들이 위치합니다.
먼저 폴더(패키지)를 다음과 같이 생성합니다.
```
- domain
  - user
  - oauth2
  - loby (메인페이지 및 기타 페이지를 목표로 제작했으나 필요시 다른 폴더 추가)
  - gameroom
  - game
```

각 폴더(패키지)에는 클래스들이 위치합니다.
클래스는 엔티티와 Dto로 구성됩니다.
우리 프로젝트의 Dto의 네이밍 규칙은 다음과 같습니다.
```
엔티티명 + 행위(CRUD) + 형태(Req/Res) + Dto
(단, Read는 Get으로 대체합니다.)
CRUD로 표현할 수 없는 경우는 행위만 표현합니다.
```
```
ex) 채팅 생성
- ChatCreateReqDto (프론트 -> 백엔드 요청)
- ChatCreateResDto (백엔드 -> 프론트 응답)

ex) 채팅 조회 (Get 요청)
- ChatGetReqDto (프론트 -> 백엔드 요청)
- ChatGetResDto (백엔드 -> 프론트 응답)

ex) 게임룸 입장 (CRUD 표현 불가)
- GameRoomJoinReqDto (프론트 -> 백엔드 요청)
- GameRoomJoinResDto (백엔드 -> 프론트 응답)
```
