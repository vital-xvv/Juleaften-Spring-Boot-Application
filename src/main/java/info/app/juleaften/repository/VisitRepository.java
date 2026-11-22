package info.app.juleaften.repository;

import info.app.juleaften.model.Visit;
import info.app.juleaften.model.dictionary.VisitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
    Optional<Visit> findByEventNameAndUserProfile_User_Email(String eventName, String email);
    List<Visit> findVisitByUserProfile_User_EmailAndStatusNotOrderByIdAscCreatedAtAsc
            (String userProfileUserEmail, VisitStatus status);
}
