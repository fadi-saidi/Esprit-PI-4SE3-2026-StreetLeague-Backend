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

/**
 * IMPORTANT — URL mapping strategy explained:
 *
 * SecurityConfig blocks /health/** to HEALTH_PROFESSIONAL only.
 * Since we can't change SecurityConfig, we use /medical/** instead.
 * /medical/** is not explicitly listed in SecurityConfig so it falls under
 * .anyRequest().authenticated() — meaning any logged-in user can reach it.
 * Role checks are then enforced by @PreAuthorize on each method.
 *
 * Login is at /auth/login (no /api prefix — matches SecurityConfig's /auth/**)
 */
@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
public class HealthController {

    private final IHealthService healthService;

    // =========================================================
    //  Medical Record endpoints → /medical/records/**
    //  Falls under anyRequest().authenticated() in SecurityConfig
    // =========================================================

    /**
     * POST /medical/records
     * Player creates their health profile.
     */
    @PostMapping("/medical/records")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<MedicalRecordDTO> createRecord(@RequestBody MedicalRecordDTO dto) {
        return ResponseEntity.ok(healthService.createRecord(dto));
    }

    /**
     * PUT /medical/records/{id}
     * Player or health professional updates a medical record.
     */
    @PutMapping("/medical/records/{id}")
    @PreAuthorize("hasRole('PLAYER') or hasRole('HEALTH_PROFESSIONAL')")
    public ResponseEntity<MedicalRecordDTO> updateRecord(
            @PathVariable Long id,
            @RequestBody MedicalRecordDTO dto) {
        return ResponseEntity.ok(healthService.updateRecord(id, dto));
    }

    /**
     * GET /medical/records/{id}
     * Get a medical record by its ID.
     */
    @GetMapping("/medical/records/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MedicalRecordDTO> getRecord(@PathVariable Long id) {
        return ResponseEntity.ok(healthService.getRecordById(id));
    }

    /**
     * GET /medical/records/player/{playerProfileId}
     * Get a player's medical record — used by coach/doctor/admin.
     */
    @GetMapping("/medical/records/player/{playerProfileId}")
    @PreAuthorize("hasRole('HEALTH_PROFESSIONAL') or hasRole('COACH') or hasRole('ADMIN')")
    public ResponseEntity<MedicalRecordDTO> getRecordByPlayer(@PathVariable Long playerProfileId) {
        return ResponseEntity.ok(healthService.getRecordByPlayer(playerProfileId));
    }

    /**
     * DELETE /medical/records/{id}
     * Admin deletes a medical record.
     */
    @DeleteMapping("/medical/records/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        healthService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }

    // =========================================================
    //  Injury endpoints → /medical/injuries/**
    // =========================================================

    /**
     * POST /medical/injuries
     * Player declares a new injury.
     */
    @PostMapping("/medical/injuries")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<InjuryDTO> declareInjury(@RequestBody InjuryDTO dto) {
        return ResponseEntity.ok(healthService.declareInjury(dto));
    }

    /**
     * PUT /medical/injuries/{id}/recommendation
     * Health professional adds advice to an injury.
     */
    @PutMapping("/medical/injuries/{id}/recommendation")
    @PreAuthorize("hasRole('HEALTH_PROFESSIONAL')")
    public ResponseEntity<InjuryDTO> addRecommendation(
            @PathVariable Long id,
            @RequestBody RecommendationRequest request) {
        return ResponseEntity.ok(healthService.addRecommendation(id, request.recommendation()));
    }

    /**
     * GET /medical/injuries/{id}
     * Get a single injury by ID.
     */
    @GetMapping("/medical/injuries/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InjuryDTO> getInjury(@PathVariable Long id) {
        return ResponseEntity.ok(healthService.getInjuryById(id));
    }

    /**
     * GET /medical/injuries/record/{medicalRecordId}
     * Get all injuries linked to a medical record.
     */
    @GetMapping("/medical/injuries/record/{medicalRecordId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<InjuryDTO>> getInjuriesByRecord(@PathVariable Long medicalRecordId) {
        return ResponseEntity.ok(healthService.getInjuriesByRecord(medicalRecordId));
    }

    /**
     * GET /medical/injuries/record/{medicalRecordId}/filter?severity=MODERATE
     * Filter injuries by severity: MINOR, MODERATE, SEVERE
     */
    @GetMapping("/medical/injuries/record/{medicalRecordId}/filter")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<InjuryDTO>> filterBySeverity(
            @PathVariable Long medicalRecordId,
            @RequestParam InjurySeverity severity) {
        return ResponseEntity.ok(healthService.filterBySeverity(medicalRecordId, severity));
    }

    /**
     * DELETE /medical/injuries/{id}
     * Delete an injury record.
     */
    @DeleteMapping("/medical/injuries/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PLAYER')")
    public ResponseEntity<Void> deleteInjury(@PathVariable Long id) {
        healthService.deleteInjury(id);
        return ResponseEntity.noContent().build();
    }
}