package likelion.hufsglobal.lgtu.runwithmate.domain.game;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor
public class StartCheckResDto {
    private final String type = "start_check";
    private boolean isStarted;
    private List<Map<String, Object>> pointBoxes;
    private List<Map<String, Object>> scoreBoxes;
    private Integer timeLeft;
}
