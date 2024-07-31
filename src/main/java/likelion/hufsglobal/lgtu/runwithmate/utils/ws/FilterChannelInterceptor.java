package likelion.hufsglobal.lgtu.runwithmate.utils.ws;

import likelion.hufsglobal.lgtu.runwithmate.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@Slf4j
@RequiredArgsConstructor
public class FilterChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        assert headerAccessor != null;
        if (String.valueOf(headerAccessor.getNativeHeader("Authorization")).equals("null")) {
            log.error("Authorization header is null");
            return message;
        }
        log.info("Authorization header !!!");
        String token = String.valueOf(headerAccessor.getNativeHeader("Authorization").get(0));
        String userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);


        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        /**
         * 사용은 안했지만 참고하면 좋은 사이트
         * https://shout-to-my-mae.tistory.com/396
         * https://velog.io/@tlatldms/Spring-Boot-STOMP-JWT-Socket-%EC%9D%B8%EC%A6%9D%ED%95%98%EA%B8%B0
         */



        return message;
    }
}