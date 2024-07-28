package likelion.hufsglobal.lgtu.runwithmate.controller;

import likelion.hufsglobal.lgtu.runwithmate.domain.game.UserPosition;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.dto.BoxRemoveResDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.dto.PositionUpdateResDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.dto.StartCheckReqDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.dto.StartCheckResDto;
import likelion.hufsglobal.lgtu.runwithmate.service.GameService;
import lombok.RequiredArgsConstructor;
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
    public StartCheckResDto checkStart(@DestinationVariable String roomId, UserPosition userPosition) {
        // TODO: JWT에서 유저 이름 가져오기
        String userId = "user1"; // 임시로 유저 이름 지정
        return gameService.checkStart(roomId, userId, userPosition);
    }

    @MessageMapping("/update_position/{roomId}")
    @SendTo("/room/{roomId}")
    public PositionUpdateResDto updatePosition(@DestinationVariable String roomId, UserPosition userPosition) {
        // TODO: JWT에서 유저 이름 가져오기, 서비스 연동하기
        String userId = "user1"; // 임시로 유저 이름 지정
        return new PositionUpdateResDto();
    }

}
