package likelion.hufsglobal.lgtu.runwithmate.controller;

import likelion.hufsglobal.lgtu.runwithmate.domain.oauth2.CustomOAuth2User;
import likelion.hufsglobal.lgtu.runwithmate.domain.user.CheckUpdateResDto;
import likelion.hufsglobal.lgtu.runwithmate.service.GameService;
import likelion.hufsglobal.lgtu.runwithmate.service.OAuth2UserService;
import likelion.hufsglobal.lgtu.runwithmate.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LobyController {

    private final GameService gameService;

    @PostMapping("/api/users/check")
    public ResponseEntity<CheckUpdateResDto> updateCheck() {
        CustomOAuth2User oAuth2User = JwtUtil.getOAuth2User();
        String userId = oAuth2User.getUserId();
        return ResponseEntity.ok(gameService.updateCheck(userId));
    }
}