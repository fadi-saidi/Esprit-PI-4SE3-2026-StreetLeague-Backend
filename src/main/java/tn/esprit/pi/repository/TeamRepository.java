package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.domain.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
