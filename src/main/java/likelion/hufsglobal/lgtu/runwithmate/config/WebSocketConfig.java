package likelion.hufsglobal.lgtu.runwithmate.config;

import likelion.hufsglobal.lgtu.runwithmate.utils.JwtUtil;
import likelion.hufsglobal.lgtu.runwithmate.utils.ws.JwtHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/send");  // 클라이언트에서 보낸 메시지를 받을 prefix
        registry.enableSimpleBroker("/room");  // 해당 주소를 구독하고 있는 클라이언트들에게 메시지 전달
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/connect")
                .addInterceptors(new JwtHandshakeInterceptor(jwtUtil))
                .setAllowedOriginPatterns("*");
    }

    /**
     * 추후 학습용 주석
     * message를 중간에 Intercept하려면
     *
     * @Override
     * public void configureClientInboundChannel(ChannelRegistration registration) {
     *    registration.interceptors(new WebSocketChannelInterceptor());
     */
}