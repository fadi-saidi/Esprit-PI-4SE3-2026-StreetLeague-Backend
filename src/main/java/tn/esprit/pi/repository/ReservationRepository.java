package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.domain.Reservation;
import tn.esprit.pi.domain.Venue;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser_Id(Long userId);
    List<Reservation> findByVenue_Id(Long venueId);
    List<Reservation> findByVenue_VenueOwnerProfile_Id(Long ownerId);
}
