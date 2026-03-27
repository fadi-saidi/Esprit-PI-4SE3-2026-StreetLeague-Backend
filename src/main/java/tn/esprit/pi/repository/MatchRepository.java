package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.domain.Match;

public interface MatchRepository extends JpaRepository<Match, Long> {
}
