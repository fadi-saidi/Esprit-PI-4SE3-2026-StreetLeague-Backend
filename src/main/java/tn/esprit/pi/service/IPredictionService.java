package tn.esprit.pi.service;

import tn.esprit.pi.dto.FantasyStatsDto;
import tn.esprit.pi.dto.LeaderboardEntryDto;
import tn.esprit.pi.dto.PlayerStatDto;
import tn.esprit.pi.dto.PredictionDto;
import tn.esprit.pi.dto.PredictionResponse;

import java.util.List;
import java.util.Optional;

public interface IPredictionService {

    PredictionResponse submitPrediction(PredictionDto request);

    Optional<PredictionResponse> getCurrentPrediction(Long virtualTeamId);

    List<PredictionResponse> getPredictionHistory(Long virtualTeamId);

    void savePlayerStat(PlayerStatDto dto);

    void resolveLastWeekPredictions();

    PredictionResponse resolvePredictionById(Long predictionId);

    List<PredictionResponse> getAllPendingPredictions();

    List<PredictionResponse> getAllPredictions();

    /** Resolve all PENDING predictions for a given week — called after match results are submitted */
    List<PredictionResponse> resolveWeekPredictions(int weekNumber, int weekYear);

    /** Admin dashboard: aggregated stats for current week */
    FantasyStatsDto getAdminStats();

    /** Leaderboard: all users ranked by wallet points */
    List<LeaderboardEntryDto> getLeaderboard();
}