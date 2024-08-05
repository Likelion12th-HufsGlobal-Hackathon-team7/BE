package likelion.hufsglobal.lgtu.runwithmate.domain.user;

import likelion.hufsglobal.lgtu.runwithmate.domain.user.type.statusType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter
@NoArgsConstructor
public class CheckUpdateResDto {
    private statusType status;
    private String type = "modal";
    private String message;
}
