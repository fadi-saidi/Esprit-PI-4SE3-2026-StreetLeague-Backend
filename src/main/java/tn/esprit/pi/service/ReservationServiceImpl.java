package tn.esprit.pi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.ReservationDTO;
import tn.esprit.pi.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl {

    private final ReservationRepository reservationRepository;
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;

    private ReservationDTO toDTO(Reservation r) {
        return ReservationDTO.builder()
                .id(r.getId())
                .venueId(r.getVenue() != null ? r.getVenue().getId() : null)
                .venueName(r.getVenue() != null ? r.getVenue().getName() : null)
                .venueAddress(r.getVenue() != null ? r.getVenue().getAddress() : null)
                .userId(r.getUser() != null ? r.getUser().getId() : null)
                .userName(r.getUser() != null ? r.getUser().getUsername() : null)
                .date(r.getDate())
                .duration(r.getDuration())
                .price(r.getPrice())
                .status(r.getStatus())
                .build();
    }

    // ── Player: book a venue ─────────────────────────────────────────────────

    public ReservationDTO book(Long venueId, LocalDateTime date, Integer duration, String email) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found"));
        if (Boolean.FALSE.equals(venue.getAvailable())) throw new RuntimeException("Venue not available");

        // Overlap check: reject if another active reservation overlaps this slot
        LocalDateTime newEnd = date.plusHours(duration);
        boolean conflict = reservationRepository.findByVenue_Id(venueId).stream()
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .anyMatch(r -> date.isBefore(r.getDate().plusHours(r.getDuration()))
                            && r.getDate().isBefore(newEnd));
        if (conflict) throw new RuntimeException("Ce terrain est déjà réservé pour cette période");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        double price = venue.getPricePerHour() != null ? venue.getPricePerHour() * duration : 0;

        Reservation res = new Reservation();
        res.setVenue(venue);
        res.setUser(user);
        res.setDate(date);
        res.setDuration(duration);
        res.setPrice(price);
        res.setStatus(ReservationStatus.PENDING);

        return toDTO(reservationRepository.save(res));
    }

    // ── Player: my reservations ───────────────────────────────────────────────

    public List<ReservationDTO> getMyReservations(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return reservationRepository.findByUser_Id(user.getId())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public void cancelMyReservation(Long id, String email) {
        Reservation res = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        if (!res.getUser().getEmail().equals(email)) throw new RuntimeException("Not your reservation");
        res.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(res);
    }

    // ── Public: verified available venues ─────────────────────────────────────

    public List<tn.esprit.pi.dto.VenueDTO> getVerifiedVenues() {
        return venueRepository.findByVerifiedTrueAndAvailableTrue()
                .stream().map(v -> tn.esprit.pi.dto.VenueDTO.builder()
                        .id(v.getId())
                        .name(v.getName())
                        .address(v.getAddress())
                        .pricePerHour(v.getPricePerHour())
                        .capacity(v.getCapacity())
                        .sportType(v.getSportType())
                        .photoUrl(v.getPhotoUrl())
                        .available(v.getAvailable())
                        .verified(v.getVerified())
                        .ownerName(v.getVenueOwnerProfile() != null && v.getVenueOwnerProfile().getUser() != null
                                ? v.getVenueOwnerProfile().getUser().getUsername() : null)
                        .build())
                .collect(Collectors.toList());
    }

    // ── Venue Owner: view / confirm / cancel + block period ───────────────────

    public List<ReservationDTO> getOwnerReservations(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return reservationRepository.findByVenue_VenueOwnerProfile_Id(user.getId())
                .stream()
                .filter(r -> r.getStatus() != ReservationStatus.BLOCKED)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public void ownerConfirmReservation(Long id, String email) {
        Reservation res = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        if (!res.getVenue().getVenueOwnerProfile().getUser().getEmail().equals(email))
            throw new RuntimeException("Access denied");
        if (res.getStatus() != ReservationStatus.PENDING)
            throw new RuntimeException("Only PENDING reservations can be confirmed");
        res.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(res);
    }

    public void ownerCancelReservation(Long id, String email) {
        Reservation res = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        if (!res.getVenue().getVenueOwnerProfile().getUser().getEmail().equals(email))
            throw new RuntimeException("Access denied");
        res.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(res);
    }

    public ReservationDTO blockPeriod(Long venueId, LocalDateTime start, Integer duration, String reason, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Venue venue = venueRepository.findByIdAndVenueOwnerProfile_Id(venueId, user.getId())
                .orElseThrow(() -> new RuntimeException("Venue not found or access denied"));

        // Check no existing reservation conflicts with this block
        LocalDateTime blockEnd = start.plusHours(duration);
        boolean conflict = reservationRepository.findByVenue_Id(venueId).stream()
                .filter(r -> r.getStatus() == ReservationStatus.PENDING || r.getStatus() == ReservationStatus.CONFIRMED)
                .anyMatch(r -> start.isBefore(r.getDate().plusHours(r.getDuration()))
                            && r.getDate().isBefore(blockEnd));
        if (conflict) throw new RuntimeException("Ce créneau est déjà réservé par un joueur");

        Reservation block = new Reservation();
        block.setVenue(venue);
        block.setUser(user);   // owner creates it
        block.setDate(start);
        block.setDuration(duration);
        block.setPrice(0.0);
        block.setStatus(ReservationStatus.BLOCKED);
        return toDTO(reservationRepository.save(block));
    }

    public void removeBlock(Long id, String email) {
        Reservation res = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
        if (!res.getVenue().getVenueOwnerProfile().getUser().getEmail().equals(email))
            throw new RuntimeException("Access denied");
        if (res.getStatus() != ReservationStatus.BLOCKED)
            throw new RuntimeException("Not a blocked period");
        reservationRepository.delete(res);
    }

    public List<ReservationDTO> getOwnerBlockedPeriods(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return reservationRepository.findByVenue_VenueOwnerProfile_Id(user.getId())
                .stream()
                .filter(r -> r.getStatus() == ReservationStatus.BLOCKED)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Public: reservations for a specific venue (for calendar) ─────────────

    public List<ReservationDTO> getVenueReservations(Long venueId) {
        return reservationRepository.findByVenue_Id(venueId).stream()
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Admin: all reservations ───────────────────────────────────────────────

    public List<ReservationDTO> getAllReservations() {
        return reservationRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public void confirmReservation(Long id) {
        Reservation res = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        res.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(res);
    }

    public void cancelReservation(Long id) {
        Reservation res = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        res.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(res);
    }

    public void verifyVenue(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found"));
        venue.setVerified(true);
        venueRepository.save(venue);
    }

    public void unverifyVenue(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found"));
        venue.setVerified(false);
        venueRepository.save(venue);
    }
}
