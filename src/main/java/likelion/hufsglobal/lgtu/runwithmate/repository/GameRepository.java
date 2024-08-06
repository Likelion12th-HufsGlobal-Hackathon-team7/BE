package likelion.hufsglobal.lgtu.runwithmate.repository;

import likelion.hufsglobal.lgtu.runwithmate.domain.game.GameInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<GameInfo, Long> {
    @Query("SELECT g FROM GameInfo g JOIN g.usersInfo u WHERE u.userId = :userId")
    List<GameInfo> findAllByUserId(@Param("userId") String userId);
}