package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.domain.Tournament;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
}
