package tn.esprit.pi.gestiontournoi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.DecisionRequest;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.EventRequest;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.EventResponse;
import tn.esprit.pi.gestiontournoi.service.GestionEventService;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@RequiredArgsConstructor
public class GestionEventController {

    private final GestionEventService service;

    @GetMapping
    public List<EventResponse> getAll(Authentication authentication) {
        return service.getApproved(authentication);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public EventResponse create(@Valid @RequestBody EventRequest request, Authentication authentication) {
        return service.submit(request, authentication);
    }

    @GetMapping("/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public List<EventResponse> getRequests(Authentication authentication) {
        return service.getRequests(authentication);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EventResponse update(@PathVariable Long id, @Valid @RequestBody EventRequest request, Authentication authentication) {
        return service.update(id, request, authentication);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public EventResponse approve(@PathVariable Long id, @RequestBody(required = false) DecisionRequest request, Authentication authentication) {
        return service.approve(id, request, authentication);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public EventResponse reject(@PathVariable Long id, @RequestBody(required = false) DecisionRequest request, Authentication authentication) {
        return service.reject(id, request, authentication);
    }

    @PostMapping("/{id}/participate")
    @PreAuthorize("isAuthenticated()")
    public EventResponse participate(@PathVariable Long id, Authentication authentication) {
        return service.participate(id, authentication);
    }

    @DeleteMapping("/{id}/participate")
    @PreAuthorize("isAuthenticated()")
    public EventResponse cancelParticipation(@PathVariable Long id, Authentication authentication) {
        return service.cancelParticipation(id, authentication);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
