package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.dto.VenueDTO;
import tn.esprit.pi.service.IVenueService;

import java.util.List;

@RestController
@RequestMapping("/venue")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENUE_OWNER')")
public class VenueController {

    private final IVenueService venueService;

    private String getConnectedEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // POST /api/venues/create
    @PostMapping("/create")
    public ResponseEntity<VenueDTO> create(@RequestBody VenueDTO dto) {
        return ResponseEntity.ok(venueService.createVenue(dto, getConnectedEmail()));
    }

    // GET /api/venues/my-venues
    @GetMapping("/my-venues")
    public ResponseEntity<List<VenueDTO>> getMyVenues() {
        return ResponseEntity.ok(venueService.getMyVenues(getConnectedEmail()));
    }

    // GET /api/venues/details/{id}
    @GetMapping("/details/{id}")
    public ResponseEntity<VenueDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(venueService.getVenueById(id, getConnectedEmail()));
    }

    // PUT /api/venues/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<VenueDTO> update(@PathVariable Long id, @RequestBody VenueDTO dto) {
        return ResponseEntity.ok(venueService.updateVenue(id, dto, getConnectedEmail()));
    }

    // DELETE /api/venues/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        venueService.deleteVenue(id, getConnectedEmail());
        return ResponseEntity.noContent().build();
    }
}