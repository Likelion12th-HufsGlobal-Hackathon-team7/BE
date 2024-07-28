package likelion.hufsglobal.lgtu.runwithmate.domain.game.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.BoxInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class StartCheckResDto {
    private final String type = "start_check";
    private boolean isStarted;
    private List<BoxInfo> pointBoxes;
    private List<BoxInfo> dopamineBoxes;
    private Long timeLeft;
}
