package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.domain.Venue;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}
