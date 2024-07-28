package likelion.hufsglobal.lgtu.runwithmate.controller;

import likelion.hufsglobal.lgtu.runwithmate.domain.game.*;
import likelion.hufsglobal.lgtu.runwithmate.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @MessageMapping("/start_check/{roomId}")
    @SendTo("/room/{roomId}")
    public StartCheckResDto checkStart(@DestinationVariable String roomId, StartCheckReqDto startCheckReqDto) {
        return gameService.checkStart(roomId, startCheckReqDto.getPosition());
    }
//
//    @MessageMapping("/update_position/{roomId}")
//    @SendTo("/room/{roomId}")
//    public PositionUpdateResDto updatePosition(@DestinationVariable String roomId, PositionUpdateReqDto positionUpdateReqDto) {
//        String userId = "user1"; // 임시로 유저 이름 지정
//        return gameService.updatePosition(roomId, userId);
//    }
//
//    @SendTo("/room/{roomId}")
//    public BoxRemoveResDto removeBox(@DestinationVariable String roomId) {
//        String userId = "user1"; // 임시로 유저 이름 지정
//        return gameService.removeBox(roomId, userId);
//    }
}
