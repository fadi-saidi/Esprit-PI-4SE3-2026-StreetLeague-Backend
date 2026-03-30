package tn.esprit.pi.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int points;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    @JsonIgnoreProperties({"wallet", "password", "enabled", "authorities",
            "accountNonExpired", "accountNonLocked", "credentialsNonExpired",
            "adminProfile", "coachProfile", "healthProfessionalProfile",
            "playerProfile", "refereeProfile", "sponsorProfile", "venueOwnerProfile"})
    private User user;
}