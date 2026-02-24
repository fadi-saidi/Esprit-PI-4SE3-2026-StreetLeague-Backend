package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}