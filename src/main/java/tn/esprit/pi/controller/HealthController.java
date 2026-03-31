package tn.esprit.pi.controller;

import jakarta.validation.Valid; // IMPORT INDISPENSABLE
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.domain.InjurySeverity;
import tn.esprit.pi.dto.Dtos.InjuryDTO;
import tn.esprit.pi.dto.Dtos.MedicalRecordDTO;
import tn.esprit.pi.dto.Dtos.RecommendationRequest;
import tn.esprit.pi.service.health.IHealthService;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
public class HealthController {

    private final IHealthService healthService;

    @PostMapping("/medical/records")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<MedicalRecordDTO> createRecord(@Valid @RequestBody MedicalRecordDTO dto) {
        return ResponseEntity.ok(healthService.createRecord(dto));
    }

    @PutMapping("/medical/records/{id}")
    @PreAuthorize("hasRole('PLAYER') or hasRole('HEALTH_PROFESSIONAL')")
    public ResponseEntity<MedicalRecordDTO> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody MedicalRecordDTO dto) {
        return ResponseEntity.ok(healthService.updateRecord(id, dto));
    }

    @PostMapping("/medical/injuries")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<InjuryDTO> declareInjury(@Valid @RequestBody InjuryDTO dto) {
        return ResponseEntity.ok(healthService.declareInjury(dto));
    }

    @PutMapping("/medical/injuries/{id}/recommendation")
    @PreAuthorize("hasRole('HEALTH_PROFESSIONAL')")
    public ResponseEntity<InjuryDTO> addRecommendation(
            @PathVariable Long id,
            @Valid @RequestBody RecommendationRequest request) {
        return ResponseEntity.ok(healthService.addRecommendation(id, request.recommendation()));
    }

    // ... (Les méthodes GET et DELETE restent inchangées car elles n'ont pas de RequestBody)
}