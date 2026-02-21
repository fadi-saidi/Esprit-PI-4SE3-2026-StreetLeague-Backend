package tn.esprit.pi.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sponsorship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Double amount;
    private LocalDate startDate;
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    private SponsorshipStatus status;
    
    @ManyToOne
    @JoinColumn(name = "sponsor_id")
    private Sponsor sponsor;
    
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
    
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
    
    @ManyToOne
    @JoinColumn(name = "venue_id")
    private Venue venue;
}
