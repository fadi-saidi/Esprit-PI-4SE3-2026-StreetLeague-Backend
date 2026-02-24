package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.dto.VenueDTO;
import tn.esprit.pi.dto.VenueOwnerWithVenuesDTO;
import tn.esprit.pi.service.IAdminVenueService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/venues")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminVenueController {

    private final IAdminVenueService adminVenueService;

    // ─── Owners avec leurs venues ─────────────────────────────────────────────

    // Voir tous les owners (chacun avec ses venues en dessous)
    @GetMapping("/owners")
    public ResponseEntity<List<VenueOwnerWithVenuesDTO>> getAllOwnersWithVenues() {
        return ResponseEntity.ok(adminVenueService.getAllOwnersWithVenues());
    }

    // Voir un owner specifique avec ses venues
    @GetMapping("/owners/{ownerId}")
    public ResponseEntity<VenueOwnerWithVenuesDTO> getOwnerWithVenues(@PathVariable Long ownerId) {
        return ResponseEntity.ok(adminVenueService.getOwnerWithVenues(ownerId));
    }

    // Verifier un owner
    @PutMapping("/owners/{ownerId}/verify")
    public ResponseEntity<VenueOwnerWithVenuesDTO> verifyOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(adminVenueService.verifyOwner(ownerId));
    }

    // Deverifier un owner
    @PutMapping("/owners/{ownerId}/unverify")
    public ResponseEntity<VenueOwnerWithVenuesDTO> unverifyOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(adminVenueService.unverifyOwner(ownerId));
    }

    // Supprimer un owner
    @DeleteMapping("/owners/{ownerId}/delete")
    public ResponseEntity<Void> deleteOwner(@PathVariable Long ownerId) {
        adminVenueService.deleteOwner(ownerId);
        return ResponseEntity.noContent().build();
    }

    // ─── Actions sur les venues (modifier / supprimer seulement) ─────────────

    // Modifier une venue
    @PutMapping("/{venueId}/update")
    public ResponseEntity<VenueDTO> updateVenue(
            @PathVariable Long venueId,
            @RequestBody VenueDTO dto
    ) {
        return ResponseEntity.ok(adminVenueService.updateVenue(venueId, dto));
    }

    // Supprimer une venue
    @DeleteMapping("/{venueId}/delete")
    public ResponseEntity<Void> deleteVenue(@PathVariable Long venueId) {
        adminVenueService.deleteVenue(venueId);
        return ResponseEntity.noContent().build();
    }
}