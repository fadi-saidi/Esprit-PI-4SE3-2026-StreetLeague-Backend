package tn.esprit.pi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.SponsorshipDTOs.*;
import tn.esprit.pi.repository.*;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.domain.SponsorProfile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;

@RestController
@CrossOrigin("*")
@RequestMapping("/sponsorships")
@RequiredArgsConstructor
public class SponsorshipController {

    private final SponsorshipRepository sponsorshipRepository;
    private final SponsorProfileRepository sponsorProfileRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final EventRepository eventRepository;
    private final TournamentRepository tournamentRepository;
    private final VenueRepository venueRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.base-url}")
    private String baseUrl;

    // ─── Public Endpoints ─────────────────────────────────────────────────────

    @GetMapping
    public List<Map<String, Object>> getAllSponsorships() {
        return sponsorshipRepository.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sponsorship> getSponsorshipById(@PathVariable Long id) {
        return sponsorshipRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/team/{teamId}")
    public List<Sponsorship> getSponsorshipsByTeam(@PathVariable Long teamId) {
        return sponsorshipRepository.findByTeamId(teamId);
    }

    @GetMapping("/event/{eventId}")
    public List<Sponsorship> getSponsorshipsByEvent(@PathVariable Long eventId) {
        return sponsorshipRepository.findByEventId(eventId);
    }

    @GetMapping("/venue/{venueId}")
    public List<Sponsorship> getSponsorshipsByVenue(@PathVariable Long venueId) {
        return sponsorshipRepository.findByVenueId(venueId);
    }

    @GetMapping("/pending")
    public List<Map<String, Object>> getPendingRequests() {
        return sponsorshipRepository.findByStatus(SponsorshipStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    @GetMapping("/active")
    public List<Map<String, Object>> getActiveSponsorship() {
        return sponsorshipRepository.findByStatus(SponsorshipStatus.ACTIVE)
                .stream().map(this::toResponse).toList();
    }

    // ─── Available Targets for Sponsorship ────────────────────────────────────

    @GetMapping("/available-targets")
    public ResponseEntity<Map<String, Object>> getAvailableTargets(
            @RequestParam(required = false) SponsorshipTargetType type) {

        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> targets = new ArrayList<>();

        if (type == null || type == SponsorshipTargetType.TEAM) {
            teamRepository.findAll().forEach(team -> {
                Map<String, Object> target = new HashMap<>();
                target.put("id", team.getId());
                target.put("name", team.getName());
                target.put("type", "TEAM");
                target.put("sportType", team.getSportType() != null ? team.getSportType().name() : null);
                target.put("logo", team.getLogo());
                targets.add(target);
            });
        }

        if (type == null || type == SponsorshipTargetType.EVENT) {
            eventRepository.findAll().stream()
                    .filter(event -> !(event instanceof Tournament)) // exclude tournaments, handled separately
                    .forEach(event -> {
                Map<String, Object> target = new HashMap<>();
                target.put("id", event.getId());
                target.put("name", event.getTitle());
                target.put("type", "EVENT");
                target.put("eventType", event.getClass().getSimpleName());
                target.put("sportType", event.getSportType() != null ? event.getSportType().name() : null);
                target.put("date", event.getDate() != null ? event.getDate().toString() : null);
                target.put("location", event.getLocation());
                targets.add(target);
            });
        }

        if (type == null || type == SponsorshipTargetType.TOURNAMENT) {
            tournamentRepository.findAll().forEach(tournament -> {
                Map<String, Object> target = new HashMap<>();
                target.put("id", tournament.getId());
                target.put("name", tournament.getName());
                target.put("type", "TOURNAMENT");
                target.put("sportType", tournament.getSportType() != null ? tournament.getSportType().name() : null);
                target.put("numberOfTeams", tournament.getNumberOfTeams());
                target.put("prize", tournament.getPrize());
                targets.add(target);
            });
        }

        if (type == null || type == SponsorshipTargetType.VENUE) {
            venueRepository.findAll().forEach(venue -> {
                Map<String, Object> target = new HashMap<>();
                target.put("id", venue.getId());
                target.put("name", venue.getName());
                target.put("type", "VENUE");
                target.put("sportType", venue.getSportType() != null ? venue.getSportType().name() : null);
                target.put("address", venue.getAddress());
                target.put("capacity", venue.getCapacity());
                target.put("pricePerHour", venue.getPricePerHour());
                targets.add(target);
            });
        }

        response.put("targets", targets);
        return ResponseEntity.ok(response);
    }

    // ─── Sponsor Endpoints ────────────────────────────────────────────────────

    @PostMapping("/submit")
    public ResponseEntity<?> submitSponsorshipRequest(
            @Valid @RequestBody CreateSponsorshipRequest request,
            Authentication auth) {

        if (auth == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");

        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null || user.getRole() != Role.SPONSOR)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only sponsors can submit sponsorship requests");

        SponsorProfile profile = sponsorProfileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Sponsor profile not found");

        SponsorshipTargetType resolvedType = request.resolvedTargetType();
        Long resolvedTargetId = request.resolvedTargetId();

        if (resolvedType == null || resolvedTargetId == null)
            return ResponseEntity.badRequest().body(Map.of("error", "Target (team/event/tournament/venue) is required"));

        Sponsorship sponsorship = new Sponsorship();
        sponsorship.setAmount(request.amount());
        sponsorship.setStartDate(request.startDate());
        sponsorship.setEndDate(request.endDate());
        sponsorship.setPaymentProof(request.paymentProof());
        sponsorship.setStatus(SponsorshipStatus.PENDING);
        sponsorship.setTargetType(resolvedType);
        sponsorship.setDescription(request.description());
        sponsorship.setExpectedBenefits(request.resolvedExpectedBenefits());
        sponsorship.setSponsorProfile(profile);

        switch (resolvedType) {
            case TEAM -> {
                Team team = teamRepository.findById(resolvedTargetId).orElse(null);
                if (team == null) return ResponseEntity.badRequest().body(Map.of("error", "Team not found"));
                sponsorship.setTeam(team);
            }
            case EVENT -> {
                Event event = eventRepository.findById(resolvedTargetId).orElse(null);
                if (event == null) return ResponseEntity.badRequest().body(Map.of("error", "Event not found"));
                sponsorship.setEvent(event);
            }
            case TOURNAMENT -> {
                Tournament tournament = tournamentRepository.findById(resolvedTargetId).orElse(null);
                if (tournament == null) return ResponseEntity.badRequest().body(Map.of("error", "Tournament not found"));
                sponsorship.setTournament(tournament);
            }
            case VENUE -> {
                Venue venue = venueRepository.findById(resolvedTargetId).orElse(null);
                if (venue == null) return ResponseEntity.badRequest().body(Map.of("error", "Venue not found"));
                sponsorship.setVenue(venue);
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(sponsorshipRepository.save(sponsorship)));
    }

    private Map<String, Object> toResponse(Sponsorship s) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", s.getId());
        map.put("amount", s.getAmount());
        map.put("startDate", s.getStartDate());
        map.put("endDate", s.getEndDate());
        map.put("status", s.getStatus());
        map.put("targetType", s.getTargetType());
        map.put("description", s.getDescription());
        map.put("expectedBenefits", s.getExpectedBenefits());
        map.put("paymentProof", s.getPaymentProof());
        if (s.getSponsorProfile() != null) {
            map.put("sponsorLogo", s.getSponsorProfile().getLogo());
            map.put("companyName", s.getSponsorProfile().getCompanyName());
        }
        if (s.getTeam() != null) map.put("targetName", s.getTeam().getName());
        else if (s.getEvent() != null) map.put("targetName", s.getEvent().getTitle());
        else if (s.getTournament() != null) map.put("targetName", s.getTournament().getName());
        else if (s.getVenue() != null) map.put("targetName", s.getVenue().getName());
        return map;
    }

    @GetMapping("/my-sponsorships")
    public ResponseEntity<?> getMySponsorships(Authentication auth) {
        if (auth == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        return userRepository.findByEmail(auth.getName())
                .filter(user -> user.getRole() == Role.SPONSOR)
                .flatMap(user -> sponsorProfileRepository.findByUserId(user.getId()))
                .map(profile -> ResponseEntity.ok(
                        sponsorshipRepository.findBySponsorProfile(profile)
                                .stream().map(this::toResponse).toList()))
                .orElse(ResponseEntity.ok(List.of()));
    }

    @PutMapping("/{id}/renew")
    public ResponseEntity<?> renewSponsorship(
            @PathVariable Long id,
            @Valid @RequestBody RenewSponsorshipRequest request,
            Authentication auth) {

        return sponsorshipRepository.findById(id)
                .map(sponsorship -> {
                    return userRepository.findByEmail(auth.getName())
                            .filter(user -> user.getRole() == Role.SPONSOR)
                            .flatMap(user -> sponsorProfileRepository.findByUserId(user.getId()))
                            .filter(profile -> sponsorship.getSponsorProfile().equals(profile))
                            .map(profile -> {
                                sponsorship.setEndDate(sponsorship.getEndDate().plusMonths(request.months()));
                                return ResponseEntity.ok(sponsorshipRepository.save(sponsorship));
                            })
                            .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/payment-proof")
    public ResponseEntity<?> uploadPaymentProof(
            @PathVariable Long id,
            @Valid @RequestBody PaymentProofRequest request,
            Authentication auth) {

        return sponsorshipRepository.findById(id)
                .map(sponsorship -> {
                    return userRepository.findByEmail(auth.getName())
                            .filter(user -> user.getRole() == Role.SPONSOR)
                            .flatMap(user -> sponsorProfileRepository.findByUserId(user.getId()))
                            .filter(profile -> sponsorship.getSponsorProfile().equals(profile))
                            .map(profile -> {
                                sponsorship.setPaymentProof(request.proofUrl());
                                sponsorshipRepository.save(sponsorship);
                                return ResponseEntity.ok(Map.of("message", "Payment proof uploaded successfully"));
                            })
                            .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/payment-proof-file")
    public ResponseEntity<?> uploadPaymentProofFile(
            @PathVariable Long id,
            @RequestParam MultipartFile file,
            Authentication auth) {

        if (file == null || file.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));

        Sponsorship sponsorship = sponsorshipRepository.findById(id).orElse(null);
        if (sponsorship == null) return ResponseEntity.notFound().build();

        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null || user.getRole() != Role.SPONSOR)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        SponsorProfile profile = sponsorProfileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null || !sponsorship.getSponsorProfile().equals(profile))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(dir);
            String ext = file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")
                    ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."))
                    : ".pdf";
            String filename = UUID.randomUUID() + ext;
            Files.copy(file.getInputStream(), dir.resolve(filename));
            String fileUrl = baseUrl + "/uploads/products/" + filename;
            sponsorship.setPaymentProof(fileUrl);
            sponsorshipRepository.save(sponsorship);
            return ResponseEntity.ok(Map.of("message", "Payment proof uploaded", "proofUrl", fileUrl));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to save file"));
        }
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<?> cancelSponsorship(@PathVariable Long id, Authentication auth) {
        return sponsorshipRepository.findById(id)
                .map(sponsorship -> {
                    userRepository.findByEmail(auth.getName()).ifPresent(user -> {
                        if (user.getRole() == Role.SPONSOR) {
                            sponsorProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
                                if (sponsorship.getSponsorProfile().equals(profile)) {
                                    sponsorshipRepository.delete(sponsorship);
                                }
                            });
                        }
                    });
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── Admin Endpoints ──────────────────────────────────────────────────────

    @GetMapping("/admin/stats")
    public ResponseEntity<?> getAdminStats() {
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        List<Object[]> activeStatsList = sponsorshipRepository.getStatsForPeriod(SponsorshipStatus.ACTIVE, firstDayOfMonth);
        List<Sponsorship> pending = sponsorshipRepository.findByStatus(SponsorshipStatus.PENDING);

        Long totalActive = 0L;
        Double totalAmount = 0.0;

        if (activeStatsList != null && !activeStatsList.isEmpty()) {
            Object[] activeStats = activeStatsList.get(0);
            if (activeStats != null) {
                if (activeStats.length > 0 && activeStats[0] != null) {
                    totalActive = ((Number) activeStats[0]).longValue();
                }
                if (activeStats.length > 1 && activeStats[1] != null) {
                    totalAmount = ((Number) activeStats[1]).doubleValue();
                }
            }
        }

        return ResponseEntity.ok(Map.of(
            "totalActive", totalActive,
            "totalAmountThisMonth", totalAmount,
            "totalPending", pending.size()
        ));
    }

    @PutMapping("/admin/{id}/approve")
    public ResponseEntity<?> approveSponsorshipRequest(@PathVariable Long id) {
        return sponsorshipRepository.findById(id)
                .map(s -> { s.setStatus(SponsorshipStatus.ACTIVE); return ResponseEntity.ok(toResponse(sponsorshipRepository.save(s))); })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/admin/{id}/reject")
    public ResponseEntity<?> rejectSponsorshipRequest(@PathVariable Long id) {
        return sponsorshipRepository.findById(id)
                .map(s -> { s.setStatus(SponsorshipStatus.REJECTED); return ResponseEntity.ok(toResponse(sponsorshipRepository.save(s))); })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<?> updateSponsorship(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSponsorshipRequest request) {

        return sponsorshipRepository.findById(id)
                .map(sponsorship -> {
                    if (request.amount() != null) {
                        sponsorship.setAmount(request.amount());
                    }
                    if (request.startDate() != null) {
                        sponsorship.setStartDate(request.startDate());
                    }
                    if (request.endDate() != null) {
                        sponsorship.setEndDate(request.endDate());
                    }
                    if (request.description() != null) {
                        sponsorship.setDescription(request.description());
                    }
                    if (request.expectedBenefits() != null) {
                        sponsorship.setExpectedBenefits(request.expectedBenefits());
                    }
                    return ResponseEntity.ok(sponsorshipRepository.save(sponsorship));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteSponsorshipAdmin(@PathVariable Long id) {
        return sponsorshipRepository.findById(id)
                .map(sponsorship -> {
                    sponsorshipRepository.delete(sponsorship);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
