package likelion.hufsglobal.lgtu.runwithmate.controller;

import likelion.hufsglobal.lgtu.runwithmate.domain.game.GameInfo;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.UserPosition;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.dto.GameFinishResDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.dto.PositionUpdateResDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.dto.StartCheckResDto;
import likelion.hufsglobal.lgtu.runwithmate.domain.oauth2.CustomOAuth2User;
import likelion.hufsglobal.lgtu.runwithmate.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/api/games")
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
//    @SendTo("/room/{roomId}")
    public void updatePosition(@DestinationVariable String roomId, UserPosition userPosition, Authentication authentication) {
        String userId = authentication.getName();
        gameService.updatePosition(roomId, userId, userPosition);
    }

    @MessageMapping("/surrender/{roomId}")
    @SendTo("/room/{roomId}")
    public void gameSurrender(@DestinationVariable String roomId) {

    }

    @GetMapping("/my-games")
    public ResponseEntity<List<GameInfo>> getMyGames(Authentication authentication) {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String userId = oAuth2User.getUserId();
        return ResponseEntity.ok(gameService.getAllGameInfos(userId));
    }

}
