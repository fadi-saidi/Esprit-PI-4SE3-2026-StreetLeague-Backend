package tn.esprit.pi.gestiontournoi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.DecisionRequest;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.MatchLookupResponse;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.MatchRequest;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.MatchResponse;
import tn.esprit.pi.gestiontournoi.service.GestionMatchService;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@RequiredArgsConstructor
public class GestionMatchController {

    private final GestionMatchService service;

    @GetMapping
    public List<MatchResponse> getAll(Authentication authentication) {
        return service.getApproved(authentication);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public MatchResponse create(@Valid @RequestBody MatchRequest request, Authentication authentication) {
        return service.submit(request, authentication);
    }

    @GetMapping("/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MatchResponse> getRequests(Authentication authentication) {
        return service.getRequests(authentication);
    }

    @GetMapping("/lookups")
    @PreAuthorize("isAuthenticated()")
    public MatchLookupResponse getLookups(Authentication authentication) {
        return service.getLookups(authentication);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MatchResponse update(@PathVariable Long id, @Valid @RequestBody MatchRequest request, Authentication authentication) {
        return service.update(id, request, authentication);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public MatchResponse approve(@PathVariable Long id, @RequestBody(required = false) DecisionRequest request, Authentication authentication) {
        return service.approve(id, request, authentication);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public MatchResponse reject(@PathVariable Long id, @RequestBody(required = false) DecisionRequest request, Authentication authentication) {
        return service.reject(id, request, authentication);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
