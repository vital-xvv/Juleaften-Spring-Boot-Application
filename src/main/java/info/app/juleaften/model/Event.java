package info.app.juleaften.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private String description;
    @Column(nullable = false)
    private int capacity;
    @OneToMany(mappedBy = "event")
    @JsonIgnore
    private List<Visit> visit;
}
