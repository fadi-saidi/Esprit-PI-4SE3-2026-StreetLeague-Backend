package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.pi.dto.VenueDTO;
import tn.esprit.pi.service.IVenueService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/venue")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENUE_OWNER')")
public class VenueController {

    private final IVenueService venueService;

    private String getConnectedEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // GET /venue/all — accessible to all authenticated users
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<VenueDTO>> getAllVenues() {
        return ResponseEntity.ok(venueService.getAllVenues());
    }

    @PostMapping("/create")
    public ResponseEntity<VenueDTO> create(@RequestBody VenueDTO dto) {
        return ResponseEntity.ok(venueService.createVenue(dto, getConnectedEmail()));
    }

    @GetMapping("/my-venues")
    public ResponseEntity<List<VenueDTO>> getMyVenues() {
        return ResponseEntity.ok(venueService.getMyVenues(getConnectedEmail()));
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<VenueDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(venueService.getVenueById(id, getConnectedEmail()));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<VenueDTO> update(@PathVariable Long id, @RequestBody VenueDTO dto) {
        return ResponseEntity.ok(venueService.updateVenue(id, dto, getConnectedEmail()));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        venueService.deleteVenue(id, getConnectedEmail());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/toggle-availability")
    public ResponseEntity<VenueDTO> toggleAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(venueService.toggleAvailability(id, getConnectedEmail()));
    }

    @PostMapping("/{id}/upload-photo")
    public ResponseEntity<Map<String, String>> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {

        String extension = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            extension = original.substring(original.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID() + extension;

        Path uploadDir = Paths.get("uploads/venues");
        Files.createDirectories(uploadDir);
        Files.copy(file.getInputStream(), uploadDir.resolve(fileName));

        String photoUrl = "/uploads/venues/" + fileName;
        venueService.updatePhotoUrl(id, photoUrl, getConnectedEmail());

        return ResponseEntity.ok(Map.of("photoUrl", photoUrl));
    }
}
