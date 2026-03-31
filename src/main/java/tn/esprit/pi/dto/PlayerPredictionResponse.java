package tn.esprit.pi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import tn.esprit.pi.domain.PlayerPredictionResult;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerPredictionResponse {
    private Long id;
    private Long playerId;
    private String playerName;
    private String playerPosition;
    private int playerRating;
    @JsonProperty("isCaptain")
    private boolean isCaptain;
    @JsonProperty("predictGoal")
    private boolean predictGoal;
    private Integer goalsScored;
    private Integer yellowCards;
    private Integer redCards;
    private Integer basketballPoints;
    private Boolean tennisWin;
    private PlayerPredictionResult result;
    private Double pointsEarned;
}