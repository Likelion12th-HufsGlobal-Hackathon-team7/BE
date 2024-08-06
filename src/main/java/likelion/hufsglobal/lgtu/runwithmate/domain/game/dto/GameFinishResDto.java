package likelion.hufsglobal.lgtu.runwithmate.domain.game.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import likelion.hufsglobal.lgtu.runwithmate.domain.game.type.FinishType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GameFinishResDto {
    private final String type = "game_finished";
    private FinishType finishType; // 두 개로 해야하는디 흠
    private String winner;
    private List<GameFinishInfoForUser> usersInfo;

}
