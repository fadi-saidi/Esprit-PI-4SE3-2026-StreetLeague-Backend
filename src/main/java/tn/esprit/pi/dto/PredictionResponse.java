package tn.esprit.pi.dto;

import lombok.*;
import tn.esprit.pi.domain.PredictionStatus;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionResponse {
    private Long id;
    private Long virtualTeamId;
    private int weekNumber;
    private int weekYear;
    private Long captainPlayerId;
    private PredictionStatus status;
    private Double totalPointsEarned;
    private LocalDate createdAt;
    private List<PlayerPredictionResponse> playerPredictions;
}