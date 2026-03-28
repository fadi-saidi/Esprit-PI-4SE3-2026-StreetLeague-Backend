package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.dto.VenueDTO;
import tn.esprit.pi.service.IAdminVenueService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/venues")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminVenueController {

    private final IAdminVenueService adminVenueService;

    @GetMapping
    public ResponseEntity<List<VenueDTO>> getAllVenues() {
        return ResponseEntity.ok(adminVenueService.getAllVenues());
    }

    @PostMapping("/owner/{ownerId}")
    public ResponseEntity<VenueDTO> createVenueForOwner(
            @PathVariable Long ownerId,
            @RequestBody VenueDTO dto
    ) {
        return ResponseEntity.ok(adminVenueService.createVenueForOwner(ownerId, dto));
    }

    @PutMapping("/{venueId}/owner/{ownerId}")
    public ResponseEntity<VenueDTO> updateVenueForOwner(
            @PathVariable Long venueId,
            @PathVariable Long ownerId,
            @RequestBody VenueDTO dto
    ) {
        return ResponseEntity.ok(adminVenueService.updateVenueForOwner(venueId, ownerId, dto));
    }

    @DeleteMapping("/{venueId}/owner/{ownerId}")
    public ResponseEntity<Void> deleteVenueForOwner(
            @PathVariable Long venueId,
            @PathVariable Long ownerId
    ) {
        adminVenueService.deleteVenueForOwner(venueId, ownerId);
        return ResponseEntity.noContent().build();
    }
}