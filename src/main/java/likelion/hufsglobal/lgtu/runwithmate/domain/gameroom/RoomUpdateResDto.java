package likelion.hufsglobal.lgtu.runwithmate.domain.gameroom;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RoomUpdateResDto {
    private final String type = "room_updated";
    private boolean status;
    private Long betPoint;
    private Integer timeLimit;
}
