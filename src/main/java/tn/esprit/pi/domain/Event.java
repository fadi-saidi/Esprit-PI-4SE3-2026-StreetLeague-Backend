package tn.esprit.pi.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private LocalDateTime date;
    private Integer duration;
    private String location;
    
    @Enumerated(EnumType.STRING)
    private SportType sportType;
    
    @Enumerated(EnumType.STRING)
    private EventStatus status;
    
    @OneToMany(mappedBy = "event")
    private Set<Sponsorship> sponsorships;
    
    @OneToMany(mappedBy = "event")
    private Set<Reservation> reservations;
}
