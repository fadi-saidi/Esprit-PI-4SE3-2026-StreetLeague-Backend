package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.pi.domain.Role;
import tn.esprit.pi.domain.SponsorProfile;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.repository.SponsorProfileRepository;
import tn.esprit.pi.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@CrossOrigin("*")
@RequestMapping("/sponsors")
@RequiredArgsConstructor
public class SponsorController {

    private final SponsorProfileRepository sponsorProfileRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.sponsors.dir}")
    private String uploadDir;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    @GetMapping
    public List<SponsorProfile> getAllSponsors() {
        return sponsorProfileRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SponsorProfile> getSponsorById(@PathVariable Long id) {
        return sponsorProfileRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my-profile")
    public ResponseEntity<SponsorProfile> getMyProfile(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .filter(user -> user.getRole() == Role.SPONSOR)
                .flatMap(user -> sponsorProfileRepository.findByUserId(user.getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update")
    public ResponseEntity<SponsorProfile> updateSponsor(@RequestBody SponsorProfile sponsorDetails, Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .filter(user -> user.getRole() == Role.SPONSOR)
                .flatMap(user -> sponsorProfileRepository.findByUserId(user.getId()))
                .map(profile -> {
                    profile.setCompanyName(sponsorDetails.getCompanyName());
                    profile.setLogo(sponsorDetails.getLogo());
                    profile.setContactEmail(sponsorDetails.getContactEmail());
                    profile.setBudget(sponsorDetails.getBudget());
                    return ResponseEntity.ok(sponsorProfileRepository.save(profile));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/upload-logo")
    public ResponseEntity<?> uploadLogo(
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String imageUrl) {

        if (imageUrl != null && !imageUrl.isBlank()) {
            if (!imageUrl.matches("^https?://.*"))
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid image URL"));
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        }

        if (file == null || file.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "No file or URL provided"));
        if (!ALLOWED_TYPES.contains(file.getContentType()))
            return ResponseEntity.badRequest().body(Map.of("error", "Only JPEG, PNG, WEBP, GIF allowed"));

        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(dir);
            String ext = file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")
                    ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."))
                    : ".jpg";
            String filename = UUID.randomUUID() + ext;
            Files.copy(file.getInputStream(), dir.resolve(filename));
            String savedUrl = baseUrl + "/uploads/sponsors/" + filename;
            return ResponseEntity.ok(Map.of("imageUrl", savedUrl));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to save file"));
        }
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteSponsor(@PathVariable Long id) {
        return sponsorProfileRepository.findById(id)
                .map(profile -> {
                    sponsorProfileRepository.delete(profile);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}