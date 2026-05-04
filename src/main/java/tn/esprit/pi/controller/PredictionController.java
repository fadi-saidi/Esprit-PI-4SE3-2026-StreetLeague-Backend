package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.dto.FantasyStatsDto;
import tn.esprit.pi.dto.LeaderboardEntryDto;
import tn.esprit.pi.dto.PlayerStatDto;
import tn.esprit.pi.dto.PredictionDto;
import tn.esprit.pi.dto.PredictionResponse;
import tn.esprit.pi.service.IPredictionService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final IPredictionService predictionService;

    // User soumet sa prediction pour la semaine courante
    @PostMapping("/submit")
    public PredictionResponse submitPrediction(@RequestBody PredictionDto predictionDto) {
        return predictionService.submitPrediction(predictionDto);
    }

    // Get all predictions
    @GetMapping("/all")
    public List<PredictionResponse> getAllPredictions() {
        return predictionService.getAllPredictions();
    }

    // Admin : résoudre manuellement une prediction
    @PostMapping("/admin/resolve/{predictionId}")
    public PredictionResponse resolve(@PathVariable Long predictionId) {
        return predictionService.resolvePredictionById(predictionId);
    }

    // Admin : aggregated stats for current week dashboard
    @GetMapping("/admin/stats")
    public FantasyStatsDto getAdminStats() {
        return predictionService.getAdminStats();
    }

    // Leaderboard: all users ranked by wallet points
    @GetMapping("/admin/leaderboard")
    public List<LeaderboardEntryDto> getLeaderboard() {
        return predictionService.getLeaderboard();
    }
}