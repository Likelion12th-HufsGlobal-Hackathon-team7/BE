package likelion.hufsglobal.lgtu.runwithmate.domain.oauth2;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuth2UserDTO {

    private String role;
    private String name;
    private String username;
    private String nickname;
    private String userId;
    private String image;

}