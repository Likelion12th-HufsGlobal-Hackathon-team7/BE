package likelion.hufsglobal.lgtu.runwithmate.repository;

import likelion.hufsglobal.lgtu.runwithmate.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);
}