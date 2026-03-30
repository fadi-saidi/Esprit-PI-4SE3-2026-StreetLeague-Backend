package tn.esprit.pi.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerPredictionEntryDto {
    private Long playerId;
    private String playerName;
    private String playerPosition;
    private int playerRating;
    private boolean predictGoal;
}