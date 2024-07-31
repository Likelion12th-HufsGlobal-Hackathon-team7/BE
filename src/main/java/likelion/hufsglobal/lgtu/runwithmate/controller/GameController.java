package likelion.hufsglobal.lgtu.runwithmate.controller;

import likelion.hufsglobal.lgtu.runwithmate.domain.game.UserPosition;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.dto.PositionUpdateResDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.dto.StartCheckResDto;
import likelion.hufsglobal.lgtu.runwithmate.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @MessageMapping("/start_check/{roomId}")
    @SendTo("/room/{roomId}")
    public StartCheckResDto checkStart(@DestinationVariable String roomId, UserPosition userPosition, Authentication authentication) {
        String userId = authentication.getName();
        return gameService.checkStart(roomId, userId, userPosition);
    }

    @MessageMapping("/update_position/{roomId}")
    @SendTo("/room/{roomId}")
    public PositionUpdateResDto updatePosition(@DestinationVariable String roomId, UserPosition userPosition, Authentication authentication) {
        String userId = authentication.getName();
        return gameService.updatePosition(roomId, userId, userPosition);
    }

    @MessageMapping("/surrender/{roomId}")
    @SendTo("/room/{roomId}")
    public void gameSurrender(@DestinationVariable String roomId) {

    }


}
