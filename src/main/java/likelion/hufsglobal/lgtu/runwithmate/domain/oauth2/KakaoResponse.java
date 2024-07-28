package likelion.hufsglobal.lgtu.runwithmate.domain.oauth2;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {
    private final Map<String, Object> attribute;
    private final Map<String, Object> properties;

    public KakaoResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
        this.properties = (Map<String, Object>) attribute.get("properties");
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    @Override
    public String getNickname() {
        return properties.get("nickname").toString();
    }

    @Override
    public String getImage() {
        return properties.get("profile_image").toString();
    }
}
