package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.domain.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}
