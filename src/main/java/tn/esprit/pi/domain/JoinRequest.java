package tn.esprit.pi.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "join_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @JsonIgnoreProperties({"playerProfiles", "logo", "matches", "tournaments", "sponsorships", "coachProfile"})
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    @JsonIgnoreProperties({"sponsorProfile","coachProfile","playerProfile","refereeProfile","healthProfessionalProfile","venueOwnerProfile","adminProfile","wallet","badges","posts","comments","likes","carts","cars","virtualTeams","ownedPlayers","rewards","reservations","password"})
    private User player;

    @Column
    private String type;   // REQUEST | INVITATION

    @Column
    private String status; // PENDING | ACCEPTED | REFUSED
}
