package likelion.hufsglobal.lgtu.runwithmate.controller;

import likelion.hufsglobal.lgtu.runwithmate.domain.gameroom.GameStartResDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.gameroom.RoomJoinResDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.gameroom.RoomUpdateReqDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.gameroom.RoomUpdateResDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.oauth2.CustomOAuth2User;
import likelion.hufsglobal.lgtu.runwithmate.service.GameRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class GameRoomController {

    private final GameRoomService gameRoomService;

    @PostMapping("/api/games/join")
    public ResponseEntity<String> joinGameRoom(Authentication authentication) {
        // authentication의 payload 영역에서 userId 가져옴
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String userId = oAuth2User.getUserId();
        System.out.println("userId: " + userId);
        return ResponseEntity.ok(gameRoomService.createRoom(userId));
    }

    @MessageMapping("/update_room/{roomId}")
    @SendTo("/room/{roomId}")
    public RoomUpdateResDto updateRoom(@DestinationVariable String roomId, RoomUpdateReqDto roomUpdateReqDto) {
        String userId = "user1"; // 요청한 유저의 id를 받아와야 함
        return gameRoomService.updateRoom(roomId, userId, roomUpdateReqDto.getBetPoint(), roomUpdateReqDto.getTimeLimit());
    }

    @MessageMapping("/join_room/{roomId}")
    @SendTo("/room/{roomId}")
    public RoomJoinResDto joinRoom(@DestinationVariable String roomId) {

        String userId = "user2"; // 임시로 유저 이름 지정
        return gameRoomService.joinRoom(roomId, userId);
    }

    @MessageMapping("/start_game/{roomId}")
    @SendTo("/room/{roomId}")
    public GameStartResDto startGame(@PathVariable String roomId) {
        // 박스 위치 정보는 GameController에서 모든 플레이어들이 게임 시작 요청하면, 위치정보를 받으면서 랜덤으로 박스 위치를 생성하여 전달
        return gameRoomService.startGame(roomId);
    }

    // ----
    @MessageMapping("/hello")
    @SendTo("/room")
    public String send(String message, Principal principal) throws Exception {
        String userId = principal.getName();
        return "User " + userId + " sent: " + message;
    }
}
