package likelion.hufsglobal.lgtu.runwithmate.repository;

import likelion.hufsglobal.lgtu.runwithmate.domain.game.GameInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<GameInfo, Long> {
    List<GameInfo> findAllByUserId(String userId);
}
