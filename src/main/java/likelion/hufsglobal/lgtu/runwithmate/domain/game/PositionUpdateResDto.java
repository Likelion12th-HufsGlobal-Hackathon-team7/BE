package likelion.hufsglobal.lgtu.runwithmate.domain.game;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter @Setter
@NoArgsConstructor
public class PositionUpdateResDto {
    private final String type="position_updated";
    private String userId;
    private Map position;
    private Integer timeRemained;
}
