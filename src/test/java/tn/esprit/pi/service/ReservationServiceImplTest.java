package tn.esprit.pi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.ReservationDTO;
import tn.esprit.pi.repository.ReservationRepository;
import tn.esprit.pi.repository.UserRepository;
import tn.esprit.pi.repository.VenueRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private VenueRepository       venueRepository;
    @Mock private UserRepository        userRepository;

    @InjectMocks private ReservationServiceImpl reservationService;

    private Venue venue;
    private User  user;

    @BeforeEach
    void setUp() {
        venue = new Venue();
        venue.setId(1L);
        venue.setName("Arena Nord");
        venue.setAddress("Tunis");
        venue.setPricePerHour(50.0);
        venue.setAvailable(true);
        venue.setVerified(true);

        user = new User();
        user.setId(10L);
        user.setUsername("alice");
        user.setEmail("alice@test.com");
    }

    // ── book ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("book: creates PENDING reservation and calculates price")
    void book_success() {
        LocalDateTime date = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        int duration = 2;

        Reservation saved = new Reservation();
        saved.setId(50L);
        saved.setVenue(venue);
        saved.setUser(user);
        saved.setDate(date);
        saved.setDuration(duration);
        saved.setPrice(100.0);
        saved.setStatus(ReservationStatus.PENDING);

        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(reservationRepository.findByVenue_Id(1L)).thenReturn(List.of());
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));
        when(reservationRepository.save(any())).thenReturn(saved);

        ReservationDTO result = reservationService.book(1L, date, duration, "alice@test.com");

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(result.getPrice()).isEqualTo(100.0);
        assertThat(result.getDuration()).isEqualTo(2);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("book: venue not available throws RuntimeException")
    void book_venueUnavailable() {
        venue.setAvailable(false);
        LocalDateTime date = LocalDateTime.now().plusDays(1);
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));

        assertThatThrownBy(() -> reservationService.book(1L, date, 1, "alice@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not available");
    }

    @Test
    @DisplayName("book: overlapping reservation throws RuntimeException")
    void book_overlap() {
        LocalDateTime existing = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime newDate  = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);

        Reservation conflict = new Reservation();
        conflict.setId(1L);
        conflict.setDate(existing);
        conflict.setDuration(3);   // covers 09:00–12:00
        conflict.setStatus(ReservationStatus.CONFIRMED);

        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(reservationRepository.findByVenue_Id(1L)).thenReturn(List.of(conflict));

        // Trying to book 10:00–12:00 (2h) — overlaps with 09:00–12:00
        assertThatThrownBy(() -> reservationService.book(1L, newDate, 2, "alice@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("réservé");
    }

    @Test
    @DisplayName("book: cancelled reservation does not cause conflict")
    void book_cancelledReservationNoConflict() {
        LocalDateTime existing = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);

        Reservation cancelled = new Reservation();
        cancelled.setId(2L);
        cancelled.setDate(existing);
        cancelled.setDuration(2);
        cancelled.setStatus(ReservationStatus.CANCELLED);

        Reservation saved = new Reservation();
        saved.setId(51L);
        saved.setVenue(venue);
        saved.setUser(user);
        saved.setDate(existing);
        saved.setDuration(2);
        saved.setPrice(100.0);
        saved.setStatus(ReservationStatus.PENDING);

        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(reservationRepository.findByVenue_Id(1L)).thenReturn(List.of(cancelled));
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));
        when(reservationRepository.save(any())).thenReturn(saved);

        ReservationDTO result = reservationService.book(1L, existing, 2, "alice@test.com");

        assertThat(result).isNotNull();
    }

    // ── ownerConfirmReservation ───────────────────────────────────────────────

    @Test
    @DisplayName("ownerConfirmReservation: sets status to CONFIRMED")
    void ownerConfirm_success() {
        User owner = new User(); owner.setId(5L); owner.setEmail("owner@test.com");
        VenueOwnerProfile ownerProfile = new VenueOwnerProfile();
        ownerProfile.setUser(owner);
        venue.setVenueOwnerProfile(ownerProfile);

        Reservation res = new Reservation();
        res.setId(1L);
        res.setVenue(venue);
        res.setStatus(ReservationStatus.PENDING);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));

        reservationService.ownerConfirmReservation(1L, "owner@test.com");

        assertThat(res.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(reservationRepository).save(res);
    }

    @Test
    @DisplayName("ownerConfirmReservation: non-owner throws RuntimeException")
    void ownerConfirm_notOwner() {
        User owner = new User(); owner.setId(5L); owner.setEmail("owner@test.com");
        User other = new User(); other.setId(9L); other.setEmail("other@test.com");
        VenueOwnerProfile ownerProfile = new VenueOwnerProfile();
        ownerProfile.setUser(owner);
        venue.setVenueOwnerProfile(ownerProfile);

        Reservation res = new Reservation();
        res.setId(1L);
        res.setVenue(venue);
        res.setStatus(ReservationStatus.PENDING);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));

        assertThatThrownBy(() -> reservationService.ownerConfirmReservation(1L, "other@test.com"))
                .isInstanceOf(RuntimeException.class);
    }

    // ── ownerCancelReservation ────────────────────────────────────────────────

    @Test
    @DisplayName("ownerCancelReservation: sets status to CANCELLED")
    void ownerCancel_success() {
        User owner = new User(); owner.setId(5L); owner.setEmail("owner@test.com");
        VenueOwnerProfile ownerProfile = new VenueOwnerProfile();
        ownerProfile.setUser(owner);
        venue.setVenueOwnerProfile(ownerProfile);

        Reservation res = new Reservation();
        res.setId(2L);
        res.setVenue(venue);
        res.setStatus(ReservationStatus.PENDING);

        when(reservationRepository.findById(2L)).thenReturn(Optional.of(res));

        reservationService.ownerCancelReservation(2L, "owner@test.com");

        assertThat(res.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    // ── cancelMyReservation ───────────────────────────────────────────────────

    @Test
    @DisplayName("cancelMyReservation: player can cancel own PENDING reservation")
    void cancelMyReservation_success() {
        Reservation res = new Reservation();
        res.setId(3L);
        res.setUser(user);
        res.setStatus(ReservationStatus.PENDING);

        when(reservationRepository.findById(3L)).thenReturn(Optional.of(res));

        reservationService.cancelMyReservation(3L, "alice@test.com");

        assertThat(res.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    // ── getMyReservations ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getMyReservations: returns reservations for user")
    void getMyReservations_success() {
        Reservation res = new Reservation();
        res.setId(10L);
        res.setUser(user);
        res.setVenue(venue);
        res.setDate(LocalDateTime.now().plusDays(1));
        res.setDuration(1);
        res.setPrice(50.0);
        res.setStatus(ReservationStatus.PENDING);

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));
        when(reservationRepository.findByUser_Id(10L)).thenReturn(List.of(res));

        List<ReservationDTO> result = reservationService.getMyReservations("alice@test.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVenueName()).isEqualTo("Arena Nord");
    }

    // ── getAllReservations (admin) ─────────────────────────────────────────────

    @Test
    @DisplayName("getAllReservations: returns all reservations")
    void getAllReservations_success() {
        Reservation res = new Reservation();
        res.setId(20L);
        res.setVenue(venue);
        res.setUser(user);
        res.setDate(LocalDateTime.now());
        res.setDuration(2);
        res.setPrice(100.0);
        res.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findAll()).thenReturn(List.of(res));

        List<ReservationDTO> result = reservationService.getAllReservations();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }
}
