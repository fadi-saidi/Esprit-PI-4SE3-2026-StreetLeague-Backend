package tn.esprit.pi.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerStatDto {
    private Long playerId;
    private String playerName;
    private String sportType; // "FOOTBALL" / "BASKETBALL" / "TENNIS"
    private int weekNumber;
    private int weekYear;
    private int goalsScored;
    private int yellowCards;
    private int redCards;
    private int basketballPoints;
    private boolean tennisWin;
}