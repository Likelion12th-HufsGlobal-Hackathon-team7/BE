package likelion.hufsglobal.lgtu.runwithmate.controller;

import likelion.hufsglobal.lgtu.runwithmate.domain.oauth2.CustomOAuth2User;
import likelion.hufsglobal.lgtu.runwithmate.domain.user.MyInfoResDto;
import likelion.hufsglobal.lgtu.runwithmate.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoginController {

    private final JwtUtil jwtUtil;

    @GetMapping("/myinfo")
    public MyInfoResDto getTokenAndUsername() {
        CustomOAuth2User oAuth2User = JwtUtil.getOAuth2User();
        String userId = oAuth2User.getUserId();
        String role = oAuth2User.getAuthorities().stream().findFirst().get().getAuthority();
        String accessToken = jwtUtil.createToken(userId, role, 60*60*60*24*30L);
        return new MyInfoResDto(userId, accessToken);
    }
}
