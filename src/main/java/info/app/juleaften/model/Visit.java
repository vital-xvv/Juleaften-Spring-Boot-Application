package info.app.juleaften.model;

import info.app.juleaften.model.dictionary.Companionship;
import info.app.juleaften.model.dictionary.VisitStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Temporal;

import java.time.LocalDateTime;

@Entity
@Table(name = "visit")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Visit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "user_profile_id")
    private UserProfile userProfile;
    private String note;
    @Enumerated(EnumType.STRING)
    private Companionship companionship;
    private Integer companionsCount;
    @Enumerated(EnumType.STRING)
    private VisitStatus status;
    @Temporal
    private LocalDateTime createdAt;
    @Temporal
    private LocalDateTime updatedAt;
    @ManyToOne
    @JoinColumn(name = "event_id", referencedColumnName = "id")
    private Event event;
}
