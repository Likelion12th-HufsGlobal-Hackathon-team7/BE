package likelion.hufsglobal.lgtu.runwithmate.domain.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
public class User {
    @Id
    private String userId;
    private String nickname;
    private String image;
    private String role;
    private Long point = 4000L;
    private LocalDateTime lastCheck;
}