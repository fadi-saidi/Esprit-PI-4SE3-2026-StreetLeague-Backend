package tn.esprit.pi.controller;

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

    // ── Medical Records ───────────────────────────────────────

    @PostMapping("/medical/records")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<MedicalRecordDTO> createRecord(@RequestBody MedicalRecordDTO dto) {
        return ResponseEntity.ok(healthService.createRecord(dto));
    }

    @PutMapping("/medical/records/{id}")
    @PreAuthorize("hasRole('PLAYER') or hasRole('HEALTH_PROFESSIONAL')")
    public ResponseEntity<MedicalRecordDTO> updateRecord(
            @PathVariable Long id,
            @RequestBody MedicalRecordDTO dto) {
        return ResponseEntity.ok(healthService.updateRecord(id, dto));
    }


    @GetMapping("/medical/records/all")
    @PreAuthorize("hasRole('HEALTH_PROFESSIONAL')")
    public ResponseEntity<List<MedicalRecordDTO>> getAllRecords() {
        return ResponseEntity.ok(healthService.getAllRecords());
    }
    @GetMapping("/medical/records/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MedicalRecordDTO> getRecord(@PathVariable Long id) {
        return ResponseEntity.ok(healthService.getRecordById(id));
    }

    @GetMapping("/medical/records/player/{playerProfileId}")
    @PreAuthorize("hasRole('PLAYER') or hasRole('HEALTH_PROFESSIONAL') or hasRole('COACH') or hasRole('ADMIN')")
    public ResponseEntity<MedicalRecordDTO> getRecordByPlayer(@PathVariable Long playerProfileId) {
        return ResponseEntity.ok(healthService.getRecordByPlayer(playerProfileId));
    }

    @DeleteMapping("/medical/records/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        healthService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }

    // ── Injuries ──────────────────────────────────────────────

    @PostMapping("/medical/injuries")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<InjuryDTO> declareInjury(@RequestBody InjuryDTO dto) {
        return ResponseEntity.ok(healthService.declareInjury(dto));
    }

    @PutMapping("/medical/injuries/{id}/recommendation")
    @PreAuthorize("hasRole('HEALTH_PROFESSIONAL')")
    public ResponseEntity<InjuryDTO> addRecommendation(
            @PathVariable Long id,
            @RequestBody RecommendationRequest request) {
        return ResponseEntity.ok(healthService.addRecommendation(id, request.recommendation()));
    }

    @GetMapping("/medical/injuries/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InjuryDTO> getInjury(@PathVariable Long id) {
        return ResponseEntity.ok(healthService.getInjuryById(id));
    }

    @GetMapping("/medical/injuries/record/{medicalRecordId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<InjuryDTO>> getInjuriesByRecord(@PathVariable Long medicalRecordId) {
        return ResponseEntity.ok(healthService.getInjuriesByRecord(medicalRecordId));
    }

    @GetMapping("/medical/injuries/record/{medicalRecordId}/filter")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<InjuryDTO>> filterBySeverity(
            @PathVariable Long medicalRecordId,
            @RequestParam InjurySeverity severity) {
        return ResponseEntity.ok(healthService.filterBySeverity(medicalRecordId, severity));
    }

    @DeleteMapping("/medical/injuries/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PLAYER')")
    public ResponseEntity<Void> deleteInjury(@PathVariable Long id) {
        healthService.deleteInjury(id);
        return ResponseEntity.noContent().build();
    }

    // ── All Injuries — Health Professional ────────────────────

    @GetMapping("/medical/injuries/all")
    @PreAuthorize("hasRole('HEALTH_PROFESSIONAL')")
    public ResponseEntity<List<InjuryDTO>> getAllInjuries() {
        return ResponseEntity.ok(healthService.getAllInjuries());
    }

}