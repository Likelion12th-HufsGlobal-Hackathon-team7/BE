package likelion.hufsglobal.lgtu.runwithmate.domain.gameroom;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RoomUpdateReqDto {
    private Long betPoint;
    private Integer timeLimit;
}
