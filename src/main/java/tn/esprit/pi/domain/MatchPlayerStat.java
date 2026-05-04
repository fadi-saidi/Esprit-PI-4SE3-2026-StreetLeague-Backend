package tn.esprit.pi.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Per-player performance stats for a specific match.
 * Stored inside the Match entity (cascade).
 * The scheduler reads these to auto-build PlayerStat records.
 */
@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MatchPlayerStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    private Long   playerId;
    private String playerName;

    @Enumerated(EnumType.STRING)
    private SportType sportType;

    // Football
    private int goalsScored;
    private int yellowCards;
    private int redCards;

    // Basketball
    private int basketballPoints;

    // Tennis
    private boolean tennisWin;
}
