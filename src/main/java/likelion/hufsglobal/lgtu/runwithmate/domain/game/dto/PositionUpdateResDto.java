package likelion.hufsglobal.lgtu.runwithmate.domain.game.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.UserPosition;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter @Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PositionUpdateResDto {
    private final String type="position_updated";
    private String userId;
    private UserPosition position;
    private Long timeLeft;
}
