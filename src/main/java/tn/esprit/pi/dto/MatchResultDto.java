package tn.esprit.pi.dto;

import lombok.Data;

import java.util.List;

/**
 * Admin submits this after a match is played.
 * The system auto-creates PlayerStat records and
 * auto-resolves all predictions for the given week.
 */
@Data
public class MatchResultDto {

    /** The match ID (must already exist in DB) */
    private Long matchId;

    /** Week this match belongs to — derived from match.weekNumber / weekYear if null */
    private Integer weekNumber;
    private Integer weekYear;

    /** One entry per player who played */
    private List<PlayerMatchStatDto> playerStats;

    @Data
    public static class PlayerMatchStatDto {
        private Long   playerId;
        private String playerName;
        private String sportType;   // FOOTBALL | BASKETBALL | TENNIS
        private String position;    // used for sport detection fallback

        // FOOTBALL
        private int goalsScored;
        private int yellowCards;
        private int redCards;

        // BASKETBALL
        private int basketballPoints;

        // TENNIS
        private boolean tennisWin;
    }
}
