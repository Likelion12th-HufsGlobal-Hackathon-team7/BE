package likelion.hufsglobal.lgtu.runwithmate.repository;

import likelion.hufsglobal.lgtu.runwithmate.domain.game.GameInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<GameInfo, Long> {
}
