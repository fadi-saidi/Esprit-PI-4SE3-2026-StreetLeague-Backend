package tn.esprit.pi.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PlayerPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "prediction_id", nullable = false)
    private Prediction prediction;

    private Long playerId;
    private String playerName;
    private String playerPosition; // GK, DEF, MID, FWD, PG, SG, SF, PF, C, P1
    private int playerRating;
    @Column(name = "is_captain", nullable = false)
    private boolean isCaptain;
    @Column(name = "predict_goal", nullable = false)
    private boolean predictGoal; // user's per-player prediction (goal/win/big game)

    // Results (filled by scheduler)
    private Integer goalsScored;       // football
    private Integer yellowCards;       // football
    private Integer redCards;          // football
    private Integer basketballPoints;  // basketball
    private Boolean tennisWin;         // tennis

    @Enumerated(EnumType.STRING)
    private PlayerPredictionResult result; // PENDING, CORRECT, WRONG, PARTIAL

    private Double pointsEarned; // final points for this player (after captain multiplier)
}