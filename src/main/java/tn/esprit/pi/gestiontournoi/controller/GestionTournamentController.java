package tn.esprit.pi.gestiontournoi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.DecisionRequest;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.TournamentRequest;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.TournamentResponse;
import tn.esprit.pi.gestiontournoi.service.GestionTournamentService;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@RequiredArgsConstructor
public class GestionTournamentController {

    private final GestionTournamentService service;

    @GetMapping
    public List<TournamentResponse> getAll(Authentication authentication) {
        return service.getApproved(authentication);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public TournamentResponse create(@Valid @RequestBody TournamentRequest request, Authentication authentication) {
        return service.submit(request, authentication);
    }

    @GetMapping("/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public List<TournamentResponse> getRequests(Authentication authentication) {
        return service.getRequests(authentication);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TournamentResponse update(@PathVariable Long id, @Valid @RequestBody TournamentRequest request, Authentication authentication) {
        return service.update(id, request, authentication);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public TournamentResponse approve(@PathVariable Long id, @RequestBody(required = false) DecisionRequest request, Authentication authentication) {
        return service.approve(id, request, authentication);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public TournamentResponse reject(@PathVariable Long id, @RequestBody(required = false) DecisionRequest request, Authentication authentication) {
        return service.reject(id, request, authentication);
    }

    @PostMapping("/{id}/participate")
    @PreAuthorize("isAuthenticated()")
    public TournamentResponse participate(@PathVariable Long id, Authentication authentication) {
        return service.participate(id, authentication);
    }

    @DeleteMapping("/{id}/participate")
    @PreAuthorize("isAuthenticated()")
    public TournamentResponse cancelParticipation(@PathVariable Long id, Authentication authentication) {
        return service.cancelParticipation(id, authentication);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
