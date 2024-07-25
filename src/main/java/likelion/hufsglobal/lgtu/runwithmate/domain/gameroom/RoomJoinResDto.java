package likelion.hufsglobal.lgtu.runwithmate.domain.gameroom;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class RoomJoinResDto {
    private final String type = "room_joined";
    private String user1;
    private String user2;
    private Long betPoint;
    private Integer timeLimit;
}
