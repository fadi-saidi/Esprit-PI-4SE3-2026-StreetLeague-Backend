package tn.esprit.pi.gestiontournoi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.DecisionRequest;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.TrainingLookupResponse;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.TrainingRequest;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.TrainingResponse;
import tn.esprit.pi.gestiontournoi.service.GestionTrainingService;

import java.util.List;

@RestController
@RequestMapping("/api/trainings")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@RequiredArgsConstructor
public class GestionTrainingController {

    private final GestionTrainingService service;

    @GetMapping
    public List<TrainingResponse> getAll(Authentication authentication) {
        return service.getApproved(authentication);
    }

    @PostMapping
    @PreAuthorize("hasRole('COACH')")
    public TrainingResponse create(@Valid @RequestBody TrainingRequest request, Authentication authentication) {
        return service.submit(request, authentication);
    }

    @GetMapping("/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public List<TrainingResponse> getRequests(Authentication authentication) {
        return service.getRequests(authentication);
    }

    @GetMapping("/lookups")
    @PreAuthorize("hasRole('COACH')")
    public TrainingLookupResponse getLookups(Authentication authentication) {
        return service.getLookups(authentication);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public TrainingResponse update(@PathVariable Long id, @Valid @RequestBody TrainingRequest request, Authentication authentication) {
        return service.update(id, request, authentication);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public TrainingResponse approve(@PathVariable Long id, @RequestBody(required = false) DecisionRequest request, Authentication authentication) {
        return service.approve(id, request, authentication);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public TrainingResponse reject(@PathVariable Long id, @RequestBody(required = false) DecisionRequest request, Authentication authentication) {
        return service.reject(id, request, authentication);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
