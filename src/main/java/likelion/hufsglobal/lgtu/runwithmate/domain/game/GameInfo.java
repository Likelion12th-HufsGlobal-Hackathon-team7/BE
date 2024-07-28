package likelion.hufsglobal.lgtu.runwithmate.domain.game;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
// @Entity
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GameInfo {
    private String roomUid;
    private String user1Uid;
    private String user2Uid;
    private Long dopamineP1;
    private Long dopamineP2;
    private Long pointP1;
    private Long pointP2;
    private Long timeLeft;
}
