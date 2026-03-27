package tn.esprit.pi.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    private String paymentProof;

    @Enumerated(EnumType.STRING)
    private SponsorshipStatus status;

    @Enumerated(EnumType.STRING)
    private SponsorshipTargetType targetType;

    @Column(length = 1000)
    private String description;

    @Column(length = 500)
    private String expectedBenefits;

    @ManyToOne
    @JoinColumn(name = "sponsor_id")
    @JsonIgnoreProperties({"sponsorships", "password", "wallet", "badges", "posts", "comments", "likes", "carts", "cars", "virtualTeams", "ownedPlayers", "rewards", "reservations"})
    private SponsorProfile sponsorProfile;

    @ManyToOne
    @JoinColumn(name = "team_id")
    @JsonIgnoreProperties({"playerProfiles", "matches", "tournaments", "sponsorships", "coachProfile"})
    private Team team;

    @ManyToOne
    @JoinColumn(name = "event_id")
    @JsonIgnoreProperties({"sponsorships", "reservations"})
    private Event event;

    @ManyToOne
    @JoinColumn(name = "tournament_id")
    @JsonIgnoreProperties({"sponsorships", "reservations", "teams", "matches"})
    private Tournament tournament;

    @ManyToOne
    @JoinColumn(name = "venue_id")
    @JsonIgnoreProperties({"sponsorships", "reservations", "venueOwnerProfile"})
    private Venue venue;
}
