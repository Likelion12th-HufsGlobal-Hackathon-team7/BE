package likelion.hufsglobal.lgtu.runwithmate.domain.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class User {
    @Id
    private String userId;
    private String nickname;
    private String image;
    private String role;
    private Long point;
    private LocalDateTime last_check;
}