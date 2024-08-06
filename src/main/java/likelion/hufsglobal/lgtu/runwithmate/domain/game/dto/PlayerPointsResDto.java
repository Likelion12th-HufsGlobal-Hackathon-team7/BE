package likelion.hufsglobal.lgtu.runwithmate.domain.game.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PlayerPointsResDto {
    private final String type = "player_points";
    private Map<String, PlayerPoint> playerPoints;
}
