package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.dto.VenueOwnerWithVenuesDTO;
import tn.esprit.pi.service.IAdminVenueOwnerService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/owners")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminVenueOwnerController {

    private final IAdminVenueOwnerService adminVenueOwnerService;

    @GetMapping
    public ResponseEntity<List<VenueOwnerWithVenuesDTO>> getAllOwnersWithVenues() {
        return ResponseEntity.ok(adminVenueOwnerService.getAllOwnersWithVenues());
    }

    @GetMapping("/{ownerId}")
    public ResponseEntity<VenueOwnerWithVenuesDTO> getOwnerWithVenues(@PathVariable Long ownerId) {
        return ResponseEntity.ok(adminVenueOwnerService.getOwnerWithVenues(ownerId));
    }

    @PutMapping("/{ownerId}/verify")
    public ResponseEntity<VenueOwnerWithVenuesDTO> verifyOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(adminVenueOwnerService.verifyOwner(ownerId));
    }

    @PutMapping("/{ownerId}/unverify")
    public ResponseEntity<VenueOwnerWithVenuesDTO> unverifyOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(adminVenueOwnerService.unverifyOwner(ownerId));
    }

    @DeleteMapping("/{ownerId}/delete")
    public ResponseEntity<Void> deleteOwner(@PathVariable Long ownerId) {
        adminVenueOwnerService.deleteOwner(ownerId);
        return ResponseEntity.noContent().build();
    }
}
