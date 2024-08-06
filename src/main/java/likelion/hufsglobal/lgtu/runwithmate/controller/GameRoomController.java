package likelion.hufsglobal.lgtu.runwithmate.controller;

import likelion.hufsglobal.lgtu.runwithmate.domain.gameroom.*;
import likelion.hufsglobal.lgtu.runwithmate.domain.oauth2.CustomOAuth2User;
import likelion.hufsglobal.lgtu.runwithmate.service.GameRoomService;
import likelion.hufsglobal.lgtu.runwithmate.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
@Slf4j
@RequiredArgsConstructor
public class GameRoomController {

    private final GameRoomService gameRoomService;

    @PostMapping("/api/games/join")
    public ResponseEntity<String> joinGameRoom() {
        CustomOAuth2User oAuth2User = JwtUtil.getOAuth2User();
        String userId = oAuth2User.getUserId();
        return ResponseEntity.ok(gameRoomService.createRoom(userId));
    }

    @MessageMapping("/update_room/{roomId}")
    @SendTo("/room/{roomId}")
    public RoomUpdateResDto updateRoom(@DestinationVariable String roomId, RoomUpdateReqDto roomUpdateReqDto, Authentication authentication) {
        String userId = authentication.getName();
        System.out.println("userId: " + userId);
        log.info("userId: " + userId);
        return gameRoomService.updateRoom(roomId, userId, roomUpdateReqDto.getBetPoint(), roomUpdateReqDto.getTimeLimit());
    }

    @MessageMapping("/check_room/{roomId}")
    @SendTo("/room/{roomId}")
    public RoomJoinResDto checkRoom(@DestinationVariable String roomId) {
        return gameRoomService.checkRoomStatus(roomId);
    }

    @MessageMapping("/join_room/{roomId}")
    @SendTo("/room/{roomId}")
    public RoomJoinResDto joinRoom(@DestinationVariable String roomId, Authentication authentication) {
        String userId = authentication.getName();
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

    @PostMapping("/api/games/test")
    public ResponseEntity<Void> test() {
        gameRoomService.createRoomForTest();
        return ResponseEntity.ok().build();
    }
}
