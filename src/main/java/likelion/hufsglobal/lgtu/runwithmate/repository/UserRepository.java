package likelion.hufsglobal.lgtu.runwithmate.repository;

import likelion.hufsglobal.lgtu.runwithmate.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserId(String userId);
}