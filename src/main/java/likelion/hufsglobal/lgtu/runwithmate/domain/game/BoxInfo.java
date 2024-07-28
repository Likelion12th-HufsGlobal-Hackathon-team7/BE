package likelion.hufsglobal.lgtu.runwithmate.domain.game;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.type.BoxType;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BoxInfo {
    private long id;
    private BoxType boxType;
    private Double lat;
    private Double lng;
    private Long boxAmount;
}
