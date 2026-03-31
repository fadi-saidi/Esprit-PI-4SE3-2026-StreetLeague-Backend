package tn.esprit.pi.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.repository.*;
import tn.esprit.pi.service.PredictionServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PredictionServiceImplTest {

    @InjectMocks
    private PredictionServiceImpl predictionService;

    @Mock
    private PredictionRepository predictionRepository;
    @Mock
    private VirtualTeamRepository virtualTeamRepository;

    @Mock
    private PlayerStatRepository playerStatRepository;

    private Prediction prediction;
    private PlayerPrediction playerPrediction;
    private VirtualTeam team;

    @BeforeEach
    void setup() {
        team = new VirtualTeam();
        team.setId(1L);
        team.setWeekPoints(0.0);
        team.setEarnedPoints(0.0);

        playerPrediction = new PlayerPrediction();
        playerPrediction.setPlayerId(10L);
        playerPrediction.setPlayerPosition("ST"); // football
        playerPrediction.setPlayerRating(10); // ✅ int (FIXED)
        playerPrediction.setPredictGoal(true);
        playerPrediction.setCaptain(false);

        prediction = new Prediction();
        prediction.setId(1L);
        prediction.setVirtualTeam(team);
        prediction.setWeekNumber(1);
        prediction.setWeekYear(2025);
        prediction.setPlayerPredictions(List.of(playerPrediction));
    }

    // ✅ Correct goal prediction
    @Test
    void shouldCalculatePointsForCorrectGoalPrediction() {
        PlayerStat stat = new PlayerStat();
        stat.setGoalsScored(1);
        stat.setYellowCards(0);
        stat.setRedCards(0);

        when(predictionRepository.findById(1L)).thenReturn(Optional.of(prediction));
        when(playerStatRepository.findByPlayerIdAndWeekNumberAndWeekYear(any(), anyInt(), anyInt()))
                .thenReturn(Optional.of(stat));

        predictionService.resolvePredictionById(1L);

        double points = prediction.getPlayerPredictions().get(0).getPointsEarned();

        assertEquals(1.5, points); // (10/10 * 1) * 1.5
    }

    // ❌ Wrong prediction (no goal)
    @Test
    void shouldApplyPenaltyForWrongPrediction() {
        PlayerStat stat = new PlayerStat();
        stat.setGoalsScored(0);

        when(predictionRepository.findById(1L)).thenReturn(Optional.of(prediction));
        when(playerStatRepository.findByPlayerIdAndWeekNumberAndWeekYear(any(), anyInt(), anyInt()))
                .thenReturn(Optional.of(stat));

        predictionService.resolvePredictionById(1L);

        double points = prediction.getPlayerPredictions().get(0).getPointsEarned();

        assertEquals(-2.0, points);
    }

    // ⚠️ Yellow card penalty
    @Test
    void shouldApplyYellowCardPenalty() {
        PlayerStat stat = new PlayerStat();
        stat.setGoalsScored(1);
        stat.setYellowCards(1);

        when(predictionRepository.findById(1L)).thenReturn(Optional.of(prediction));
        when(playerStatRepository.findByPlayerIdAndWeekNumberAndWeekYear(any(), anyInt(), anyInt()))
                .thenReturn(Optional.of(stat));

        predictionService.resolvePredictionById(1L);

        double points = prediction.getPlayerPredictions().get(0).getPointsEarned();

        assertEquals(-1.5, points); // 1.5 - 3
    }

    // 🔴 Red card penalty
    @Test
    void shouldApplyRedCardPenalty() {
        PlayerStat stat = new PlayerStat();
        stat.setGoalsScored(0);
        stat.setRedCards(1);

        when(predictionRepository.findById(1L)).thenReturn(Optional.of(prediction));
        when(playerStatRepository.findByPlayerIdAndWeekNumberAndWeekYear(any(), anyInt(), anyInt()))
                .thenReturn(Optional.of(stat));

        predictionService.resolvePredictionById(1L);

        double points = prediction.getPlayerPredictions().get(0).getPointsEarned();

        assertEquals(-8.0, points); // -2 -6
    }

    // 👑 Captain doubles positive points
    @Test
    void shouldDoublePointsForCaptain() {
        playerPrediction.setCaptain(true);

        PlayerStat stat = new PlayerStat();
        stat.setGoalsScored(1);

        when(predictionRepository.findById(1L)).thenReturn(Optional.of(prediction));
        when(playerStatRepository.findByPlayerIdAndWeekNumberAndWeekYear(any(), anyInt(), anyInt()))
                .thenReturn(Optional.of(stat));

        predictionService.resolvePredictionById(1L);

        double points = prediction.getPlayerPredictions().get(0).getPointsEarned();

        assertEquals(3.0, points); // 1.5 * 2
    }

    //  VERY IMPORTANT:
    @Test
    void shouldDoubleNegativePointsForCaptain() {
        playerPrediction.setCaptain(true);

        PlayerStat stat = new PlayerStat();
        stat.setGoalsScored(0);
        stat.setRedCards(1);

        when(predictionRepository.findById(1L)).thenReturn(Optional.of(prediction));
        when(playerStatRepository.findByPlayerIdAndWeekNumberAndWeekYear(any(), anyInt(), anyInt()))
                .thenReturn(Optional.of(stat));

        predictionService.resolvePredictionById(1L);

        double points = prediction.getPlayerPredictions().get(0).getPointsEarned();

        assertEquals(-16.0, points); // (-2 -6) * 2
    }
}