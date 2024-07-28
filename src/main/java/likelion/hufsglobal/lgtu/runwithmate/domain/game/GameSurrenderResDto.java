package likelion.hufsglobal.lgtu.runwithmate.domain.game;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class GameSurrenderResDto {
    private final String type = "game_finished";
    private String finishType; // 두 개로 해야하는디 흠
    private String winner;
    private String winnerName;
    private String loserName;
    private Integer pointP1;
    private Integer pointP2;
    private Integer dopaminP1;
    private Integer dopaminP2;
}
