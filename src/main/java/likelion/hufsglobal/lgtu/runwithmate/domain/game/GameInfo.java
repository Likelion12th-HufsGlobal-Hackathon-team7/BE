package likelion.hufsglobal.lgtu.runwithmate.domain.game;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@Entity
public class GameInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String roomId;
    private Long betPoint;
    @OneToMany(cascade = CascadeType.ALL)
    private List<GameInfoForUser> usersInfo = new ArrayList<>();
}
