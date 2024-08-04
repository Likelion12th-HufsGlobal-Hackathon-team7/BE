package likelion.hufsglobal.lgtu.runwithmate.service;

import likelion.hufsglobal.lgtu.runwithmate.domain.oauth2.CustomOAuth2User;
import likelion.hufsglobal.lgtu.runwithmate.domain.oauth2.KakaoResponse;
import likelion.hufsglobal.lgtu.runwithmate.domain.oauth2.OAuth2Response;
import likelion.hufsglobal.lgtu.runwithmate.domain.oauth2.OAuth2UserDTO;
import likelion.hufsglobal.lgtu.runwithmate.domain.user.User;
import likelion.hufsglobal.lgtu.runwithmate.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    public OAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        OAuth2Response oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());

        String userId = oAuth2Response.getProvider()+"_"+oAuth2Response.getProviderId();
        User existUser = userRepository.findByUserId(userId).orElse(null);

        if (existUser == null) {
            User newUser = new User();
            newUser.setUserId(userId);
            newUser.setNickname(oAuth2Response.getNickname());
            newUser.setImage(oAuth2Response.getImage());
            newUser.setRole("ROLE_USER");
            // 회원가입 시 포인트 0으로 설정
            newUser.setPoint(0L);
            userRepository.save(newUser);

            OAuth2UserDTO userDTO = new OAuth2UserDTO();
            userDTO.setUserId(userId);
            userDTO.setNickname(oAuth2Response.getNickname());
            userDTO.setImage(oAuth2Response.getImage());
            userDTO.setRole("ROLE_USER");

            return new CustomOAuth2User(userDTO);
        } else {
            existUser.setNickname(oAuth2Response.getNickname());
            existUser.setImage(oAuth2Response.getImage());

            userRepository.save(existUser);

            OAuth2UserDTO userDTO = new OAuth2UserDTO();
            userDTO.setUserId(existUser.getUserId());
            userDTO.setNickname(existUser.getNickname());
            userDTO.setImage(existUser.getImage());
            userDTO.setRole("ROLE_USER");

            return new CustomOAuth2User(userDTO);
        }
    }
}
