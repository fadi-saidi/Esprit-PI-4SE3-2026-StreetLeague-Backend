package tn.esprit.pi.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FantasyStatsDto {
    private double totalPointsThisWeek;
    private String mostPredictedPlayer;
    private BestPrediction bestPrediction;
    private int activeTeams;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class BestPrediction {
        private String username;
        private double points;
    }
}
