package tn.esprit.pi.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionDto {
    private Long virtualTeamId;
    private Long captainPlayerId;
    private List<PlayerPredictionEntryDto> players;
}