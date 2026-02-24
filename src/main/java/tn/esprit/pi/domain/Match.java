package tn.esprit.pi.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "sport_match")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Match extends Event {
    private String score;
    
    @ManyToMany
    @JoinTable(name = "match_team",
        joinColumns = @JoinColumn(name = "match_id"),
        inverseJoinColumns = @JoinColumn(name = "team_id"))
    private Set<Team> teams;
    
    @ManyToOne
    @JoinColumn(name = "referee_id")
    private RefereeProfile refereeProfile;
    
    @ManyToOne
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;
}
