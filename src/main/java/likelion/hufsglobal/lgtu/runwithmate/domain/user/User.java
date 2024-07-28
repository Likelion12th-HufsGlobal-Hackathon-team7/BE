package likelion.hufsglobal.lgtu.runwithmate.domain.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class User {
    @Id
    private String role;
    private String name;
    private String username;
    private String nickname;
    private String userId;
    private String image;
}
