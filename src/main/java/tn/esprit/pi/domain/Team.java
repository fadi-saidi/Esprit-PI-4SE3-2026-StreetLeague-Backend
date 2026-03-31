package tn.esprit.pi.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;

    @Column(columnDefinition = "LONGTEXT")
    private String logo;

    private Long captainId;
    private LocalDateTime createdAt;
    
    @Enumerated(EnumType.STRING)
    private SportType sportType;
    
    @ManyToMany
    @JoinTable(name = "team_player",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "player_id"))
    @JsonIgnoreProperties({"teams", "trainings", "medicalRecord"})
    private Set<PlayerProfile> playerProfiles;

    @ManyToOne
    @JoinColumn(name = "coach_id")
    @JsonIgnoreProperties({"teams", "trainings"})
    private CoachProfile coachProfile;

    @ManyToMany(mappedBy = "teams")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<Match> matches;

    @ManyToMany(mappedBy = "teams")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<Tournament> tournaments;

    @OneToMany(mappedBy = "team")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<Sponsorship> sponsorships;

    /** Virtual field for frontend compatibility: exposes players as {id, fullName, email} */
    @Transient
    @JsonProperty("players")
    public List<java.util.Map<String, Object>> getPlayers() {
        if (playerProfiles == null) return List.of();
        return playerProfiles.stream().map(p -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            if (p.getUser() != null) {
                m.put("id", p.getUser().getId());
                m.put("fullName", p.getUser().getUsername());
                m.put("email", p.getUser().getEmail());
            }
            return m;
        }).collect(Collectors.toList());
    }

    /** Virtual field for frontend compatibility: exposes sportType as "type" */
    @Transient
    @JsonProperty("type")
    public String getType() {
        return sportType != null ? sportType.name() : null;
    }
}
