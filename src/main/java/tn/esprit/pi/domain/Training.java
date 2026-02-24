package tn.esprit.pi.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Training extends Event {
    private String description;
    private Integer maxParticipants;
    
    @ManyToOne
    @JoinColumn(name = "coach_id")
    private CoachProfile coachProfile;
    
    @ManyToMany
    @JoinTable(name = "training_player",
        joinColumns = @JoinColumn(name = "training_id"),
        inverseJoinColumns = @JoinColumn(name = "player_id"))
    private Set<PlayerProfile> participantProfiles;
}
