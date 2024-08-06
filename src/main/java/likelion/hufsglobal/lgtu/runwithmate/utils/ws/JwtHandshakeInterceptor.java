package likelion.hufsglobal.lgtu.runwithmate.utils.ws;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import likelion.hufsglobal.lgtu.runwithmate.utils.JwtUtil;
import likelion.hufsglobal.lgtu.runwithmate.domain.oauth2.CustomOAuth2User;
import likelion.hufsglobal.lgtu.runwithmate.domain.oauth2.OAuth2UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Slf4j
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    public JwtHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        String token = getTokenFromRequest(request);
        log.info(request.getHeaders().toString());
        log.info("토큰: " + token);
        if (token != null && !jwtUtil.isTokenExpired(token)) {
            String userId = jwtUtil.getUserId(token);
            String role = jwtUtil.getRole(token);

            OAuth2UserDTO userDTO = new OAuth2UserDTO();
            userDTO.setUserId(userId);
            userDTO.setRole(role);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority(role)));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("1차 성공");
            return true;
        }
        log.error("1차 실패");
        return false;
    }

    private String getTokenFromRequest(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
         if (!headers.containsKey("cookie")) {
             return null;
         }
        String cookie = headers.getFirst("cookie");
        String[] cookieArr = cookie.split(";");
        for (String s : cookieArr) {
            if (s.contains("Authorization")) {
                return s.split("=")[1];
            }
        }
        return null;
//        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
//        HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
////        log.info(httpServletRequest.toString());
//        return httpServletRequest.getParameter("accessToken");
        /**
         * 나중에 학습용으로 참고하기 위한 주석
         * 헤더에 토큰이 있는 경우
         *
         * HttpHeaders headers = request.getHeaders();
         * if (headers.containsKey("Authorization")) {
         *    return headers.getFirst("Authorization");
         *    }
         * return null;
         */
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // Do nothing
    }
}