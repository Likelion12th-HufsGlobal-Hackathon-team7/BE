package likelion.hufsglobal.lgtu.runwithmate.domain.gameroom;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class GameStartResDto {
    private final String type = "game_started";
    private String roomId;
}
