package tn.esprit.pi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.MatchResultDto;
import tn.esprit.pi.repository.MatchRepository;
import tn.esprit.pi.repository.PlayerStatRepository;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class MatchService {

    private final MatchRepository         matchRepository;
    private final PlayerStatRepository    playerStatRepository;
    private final IPredictionService      predictionService;

    /**
     * Admin submits match results:
     * 1. Upsert a PlayerStat record for every player in the payload
     * 2. Auto-resolve all PENDING predictions for that week
     * Returns the number of predictions that were resolved.
     */
    public Map<String, Object> submitMatchResult(MatchResultDto dto) {

        // ── Resolve week ──────────────────────────────────────────────────────
        int weekNumber;
        int weekYear;

        if (dto.getWeekNumber() != null && dto.getWeekYear() != null) {
            weekNumber = dto.getWeekNumber();
            weekYear   = dto.getWeekYear();
        } else if (dto.getMatchId() != null) {
            Match match = matchRepository.findById(dto.getMatchId())
                    .orElseThrow(() -> new RuntimeException("Match not found: " + dto.getMatchId()));
            if (match.getWeekNumber() != null && match.getWeekYear() != null) {
                weekNumber = match.getWeekNumber();
                weekYear   = match.getWeekYear();
            } else {
                // Derive from match date
                int[] ww = weekOf(match.getDate() != null
                        ? match.getDate().toLocalDate()
                        : LocalDate.now());
                weekNumber = ww[0];
                weekYear   = ww[1];
            }
        } else {
            // Default to current week
            int[] ww = weekOf(LocalDate.now());
            weekNumber = ww[0];
            weekYear   = ww[1];
        }

        final int wn = weekNumber;
        final int wy = weekYear;

        // ── Upsert PlayerStat AND persist to Match.playerStats ───────────────
        // Saving stats in both places:
        //   • PlayerStat  → used immediately by manual resolve & admin panel
        //   • Match.playerStats → used by the Monday scheduler for auto-resolution
        int statsCount = 0;
        if (dto.getPlayerStats() != null && !dto.getPlayerStats().isEmpty()) {

            // Optionally attach stats to the match entity so the scheduler can find them
            Match match = dto.getMatchId() != null
                    ? matchRepository.findById(dto.getMatchId()).orElse(null)
                    : null;

            if (match != null) {
                // Clear old stats on this match before re-adding (idempotent)
                match.getPlayerStats().clear();
                match.setStatus(tn.esprit.pi.domain.EventStatus.FINISHED);
            }

            for (MatchResultDto.PlayerMatchStatDto s : dto.getPlayerStats()) {
                SportType sport = parseSport(s.getSportType());

                // ── 1. Upsert PlayerStat (direct, used immediately) ──────────
                PlayerStat stat = playerStatRepository
                        .findByPlayerIdAndWeekNumberAndWeekYear(s.getPlayerId(), wn, wy)
                        .orElse(new PlayerStat());

                stat.setPlayerId(s.getPlayerId());
                stat.setPlayerName(s.getPlayerName());
                stat.setWeekNumber(wn);
                stat.setWeekYear(wy);
                stat.setSportType(sport);
                stat.setGoalsScored(s.getGoalsScored());
                stat.setYellowCards(s.getYellowCards());
                stat.setRedCards(s.getRedCards());
                stat.setBasketballPoints(s.getBasketballPoints());
                stat.setTennisWin(s.isTennisWin());
                playerStatRepository.save(stat);

                // ── 2. Also attach to Match entity (used by scheduler) ───────
                if (match != null) {
                    MatchPlayerStat mps = MatchPlayerStat.builder()
                            .match(match)
                            .playerId(s.getPlayerId())
                            .playerName(s.getPlayerName())
                            .sportType(sport)
                            .goalsScored(s.getGoalsScored())
                            .yellowCards(s.getYellowCards())
                            .redCards(s.getRedCards())
                            .basketballPoints(s.getBasketballPoints())
                            .tennisWin(s.isTennisWin())
                            .build();
                    match.getPlayerStats().add(mps);
                }

                statsCount++;
            }

            if (match != null) {
                matchRepository.save(match);
            }
        }

        // ── Auto-resolve all PENDING predictions for this week ────────────────
        List<tn.esprit.pi.dto.PredictionResponse> resolved =
                predictionService.resolveWeekPredictions(wn, wy);

        return Map.of(
                "weekNumber",   wn,
                "weekYear",     wy,
                "statsRecorded", statsCount,
                "predictionsResolved", resolved.size()
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int[] weekOf(LocalDate date) {
        WeekFields wf = WeekFields.of(Locale.getDefault());
        return new int[]{ date.get(wf.weekOfWeekBasedYear()), date.getYear() };
    }

    private SportType parseSport(String s) {
        if (s == null) return SportType.FOOTBALL;
        return switch (s.toUpperCase()) {
            case "BASKETBALL" -> SportType.BASKETBALL;
            case "TENNIS"     -> SportType.TENNIS;
            default           -> SportType.FOOTBALL;
        };
    }
}
