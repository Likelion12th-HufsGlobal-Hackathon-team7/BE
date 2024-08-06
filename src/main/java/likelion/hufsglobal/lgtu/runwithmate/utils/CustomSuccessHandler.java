package likelion.hufsglobal.lgtu.runwithmate.utils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import likelion.hufsglobal.lgtu.runwithmate.domain.oauth2.CustomOAuth2User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Slf4j
@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    public CustomSuccessHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String userId = customOAuth2User.getUserId();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String token = jwtUtil.createToken(userId, role, 60*60*60*24*30L); // 10시간

        // 사용자 localhost로 접속하면 localhost로 리다이렉트, 배포 상태면 배포 사이트로 리다이렉트
        if (request.getServerName().equals("localhost")) {
            response.addCookie(createCookie("Authorization", token, "localhost"));
            response.sendRedirect("http://localhost:5173/main/");
            return;
        }

        response.addCookie(createCookie("Authorization", token, "klr.kr"));
        response.sendRedirect("https://runwithmate.klr.kr/main/");
    }

    private Cookie createCookie(String key, String value, String domain) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60*60*60*10);
        cookie.setPath("/");
        cookie.setDomain(domain);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        return cookie;
    }
}
