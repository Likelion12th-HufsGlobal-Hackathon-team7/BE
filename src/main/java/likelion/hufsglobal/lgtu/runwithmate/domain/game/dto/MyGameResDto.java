package likelion.hufsglobal.lgtu.runwithmate.domain.game.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MyGameResDto {
    private String nicknameP1;
    private String nicknameP2;
    private Integer scoreP1;
    private Integer scoreP2;
    private Integer pointP1;
    private Integer pointP2;

    // 리스트 형식으로 어케 response하게요?
}
