package tn.esprit.pi.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tournament extends Event {
    private String name;
    private Integer numberOfTeams;
    private String phase;
    private Double prize;
    
    @ManyToMany
    @JoinTable(name = "tournament_team",
        joinColumns = @JoinColumn(name = "tournament_id"),
        inverseJoinColumns = @JoinColumn(name = "team_id"))
    private Set<Team> teams;
    
    @OneToMany(mappedBy = "tournament")
    private Set<Match> matches;
}
