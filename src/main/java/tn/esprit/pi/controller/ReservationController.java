package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.dto.ReservationDTO;
import tn.esprit.pi.dto.VenueDTO;
import tn.esprit.pi.service.ReservationServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ReservationController {

    private final ReservationServiceImpl reservationService;

    private String email() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // ── Public: list verified venues ─────────────────────────────────────────

    @GetMapping("/venues")
    public List<VenueDTO> getVerifiedVenues() {
        return reservationService.getVerifiedVenues();
    }

    // ── Public: venue calendar ───────────────────────────────────────────────

    @GetMapping("/venue/{venueId}")
    public List<ReservationDTO> getVenueReservations(@PathVariable Long venueId) {
        return reservationService.getVenueReservations(venueId);
    }

    // ── Venue Owner ──────────────────────────────────────────────────────────

    @GetMapping("/owner/my")
    public List<ReservationDTO> ownerReservations() {
        return reservationService.getOwnerReservations(email());
    }

    @PutMapping("/owner/{id}/confirm")
    public ResponseEntity<Void> ownerConfirm(@PathVariable Long id) {
        reservationService.ownerConfirmReservation(id, email());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/owner/{id}/cancel")
    public ResponseEntity<Void> ownerCancel(@PathVariable Long id) {
        reservationService.ownerCancelReservation(id, email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/owner/block")
    public ReservationDTO blockPeriod(@RequestBody Map<String, Object> body) {
        Long venueId  = Long.valueOf(body.get("venueId").toString());
        LocalDateTime start = LocalDateTime.parse(body.get("start").toString());
        Integer duration    = Integer.valueOf(body.get("duration").toString());
        String reason       = body.getOrDefault("reason", "").toString();
        return reservationService.blockPeriod(venueId, start, duration, reason, email());
    }

    @DeleteMapping("/owner/block/{id}")
    public ResponseEntity<Void> removeBlock(@PathVariable Long id) {
        reservationService.removeBlock(id, email());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/owner/blocks")
    public List<ReservationDTO> ownerBlocks() {
        return reservationService.getOwnerBlockedPeriods(email());
    }

    // ── Player: book ─────────────────────────────────────────────────────────

    @PostMapping("/book")
    public ReservationDTO book(@RequestBody Map<String, Object> body) {
        Long venueId = Long.valueOf(body.get("venueId").toString());
        LocalDateTime date = LocalDateTime.parse(body.get("date").toString());
        Integer duration = Integer.valueOf(body.get("duration").toString());
        return reservationService.book(venueId, date, duration, email());
    }

    @GetMapping("/my")
    public List<ReservationDTO> myReservations() {
        return reservationService.getMyReservations(email());
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        reservationService.cancelMyReservation(id, email());
        return ResponseEntity.ok().build();
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    @GetMapping("/admin/all")
    public List<ReservationDTO> allReservations() {
        return reservationService.getAllReservations();
    }

    @PutMapping("/admin/{id}/confirm")
    public ResponseEntity<Void> confirm(@PathVariable Long id) {
        reservationService.confirmReservation(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/{id}/cancel")
    public ResponseEntity<Void> adminCancel(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/venues/{venueId}/verify")
    public ResponseEntity<Void> verifyVenue(@PathVariable Long venueId) {
        reservationService.verifyVenue(venueId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/venues/{venueId}/unverify")
    public ResponseEntity<Void> unverifyVenue(@PathVariable Long venueId) {
        reservationService.unverifyVenue(venueId);
        return ResponseEntity.ok().build();
    }
}
