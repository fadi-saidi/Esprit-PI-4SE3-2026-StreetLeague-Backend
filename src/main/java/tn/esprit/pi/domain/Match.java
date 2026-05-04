package tn.esprit.pi.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "sport_match")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match extends Event {

    private String score;

    // ── Real teams playing the match ──────────────────────
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

    // ── Fantasy: players who played in this match ─────────
    // Stores PlayerProfile IDs — used by the prediction resolver
    // to know which players were active this week
    @ElementCollection
    @CollectionTable(
            name = "match_player_ids",
            joinColumns = @JoinColumn(name = "match_id")
    )
    @Column(name = "player_id")
    @Builder.Default
    private List<Long> playerIds = new ArrayList<>();

    // ── Fantasy: week identification ──────────────────────
    // Links this match to a prediction week (same weekNumber + weekYear as Prediction)
    private Integer weekNumber;
    private Integer weekYear;

    // ── Fantasy: per-player performance in this match ─────
    // Admin fills these when entering match results.
    // The scheduler reads these to auto-build PlayerStat records.
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("match")
    @Builder.Default
    private List<MatchPlayerStat> playerStats = new ArrayList<>();
}