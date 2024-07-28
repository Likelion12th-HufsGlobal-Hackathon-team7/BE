package likelion.hufsglobal.lgtu.runwithmate.domain.game;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GameInfo {
    private String roomUid;
    private String user1Uid;
    private String user2Uid;
    private Integer scoreP1;
    private Integer scoreP2;
    private Integer pointP1;
    private Integer pointP2;
    private LocalDateTime timeLeft;
}
