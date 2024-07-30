package likelion.hufsglobal.lgtu.runwithmate.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@Slf4j
public class AuthChannelInterceptorAdapter implements ChannelInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && accessor.getCommand() != null && accessor.getCommand().getMessageType().name().equals("CONNECT")) {
            String jwtToken = accessor.getFirstNativeHeader("Authorization");
            if (jwtToken != null && !jwtToken.isEmpty()) {
                try {
                    log.info("JWT Token: {}", jwtToken);
                    String userId = jwtUtil.getUserId(jwtToken);
                    accessor.setUser(new UserPrincipal(userId)); // 사용자 ID를 Principal로 설정
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid JWT Token", e);
                }
            } else {
                throw new IllegalArgumentException("Missing JWT Token");
            }
        }

        return message;
    }
}