package likelion.hufsglobal.lgtu.runwithmate.domain.gameroom;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RoomJoinResDto {
    private final String type = "room_joined";
    private String user1;
    private String user2;
    private Long user1Point;
    private Long user2Point;
    private Long betPoint;
    private Long timeLimit;
}
