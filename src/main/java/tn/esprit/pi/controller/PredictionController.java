package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
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
    public PredictionResponse submit(@RequestBody PredictionDto request) {
        return predictionService.submitPrediction(request);
    }

    // Prediction courante d'une virtual team
    @GetMapping("/current/{virtualTeamId}")
    public Optional<PredictionResponse> getCurrent(@PathVariable Long virtualTeamId) {
        return predictionService.getCurrentPrediction(virtualTeamId);
    }

    // Historique complet d'une virtual team
    @GetMapping("/history/{virtualTeamId}")
    public List<PredictionResponse> getHistory(@PathVariable Long virtualTeamId) {
        return predictionService.getPredictionHistory(virtualTeamId);
    }

    // Admin : enregistrer les stats d'un joueur après le match
    @PostMapping("/admin/stats")
    public void saveStats(@RequestBody PlayerStatDto dto) {
        predictionService.savePlayerStat(dto);
    }

    // Admin : résoudre manuellement une prediction
    @PostMapping("/admin/resolve/{predictionId}")
    public void resolve(@PathVariable Long predictionId) {
        predictionService.resolvePredictionById(predictionId);
    }
}