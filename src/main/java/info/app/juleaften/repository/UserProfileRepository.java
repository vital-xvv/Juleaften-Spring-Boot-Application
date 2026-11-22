package info.app.juleaften.repository;

import info.app.juleaften.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUser_Email(String userEmail);
    Optional<UserProfile> findByVerificationTokenHash(String tokenHash);
}
