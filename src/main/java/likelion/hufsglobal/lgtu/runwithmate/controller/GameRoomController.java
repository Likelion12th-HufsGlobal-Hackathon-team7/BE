package likelion.hufsglobal.lgtu.runwithmate.controller;

import likelion.hufsglobal.lgtu.runwithmate.domain.gameroom.GameStartResDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.gameroom.RoomJoinResDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.gameroom.RoomUpdateReqDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.gameroom.RoomUpdateResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequiredArgsConstructor
public class GameRoomController {

    @PostMapping("/api/games/join")
    public ResponseEntity<String> joinGameRoom() {

        // 방 번호 생성 로직
        return ResponseEntity.ok("A1B2C3");

    }

    @MessageMapping("/update_room/{roomId}")
    @SendTo("/room/{roomId}")
    public RoomUpdateResDto updateRoom(@DestinationVariable String roomId, RoomUpdateReqDto roomUpdateReqDto) {

        // 방 정보 업데이트 로직
        RoomUpdateResDto roomUpdateResDto = new RoomUpdateResDto();
        roomUpdateResDto.setStatus(true);
        roomUpdateResDto.setBetPoint(roomUpdateReqDto.getBetPoint());
        roomUpdateResDto.setTimeLimit(roomUpdateReqDto.getTimeLimit());

        return roomUpdateResDto;
    }

    @MessageMapping("/join_room/{roomId}")
    @SendTo("/room/{roomId}")
    public RoomJoinResDto joinRoom(@DestinationVariable String roomId) {

        // 방 참가 로직
        RoomJoinResDto roomJoinResDto = new RoomJoinResDto();
        roomJoinResDto.setUser1("user1");
        roomJoinResDto.setUser2("user2");
        roomJoinResDto.setBetPoint(1000L);
        roomJoinResDto.setTimeLimit(60);

        return roomJoinResDto;
    }

    @MessageMapping("/start_game/{roomId}")
    @SendTo("/room/{roomId}")
    public GameStartResDto startGame(@PathVariable String roomId) {

        // 게임 시작 로직
        GameStartResDto gameStartResDto = new GameStartResDto();
        gameStartResDto.setRoomId(roomId);

        return gameStartResDto;
    }
}
