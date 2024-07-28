package likelion.hufsglobal.lgtu.runwithmate.domain.game;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class BoxRemoveResDto {
    private final String type = "box_removed";
    private String boxType;
    private String boxId;
    private Integer boxAmount;
    private String userId;
}
