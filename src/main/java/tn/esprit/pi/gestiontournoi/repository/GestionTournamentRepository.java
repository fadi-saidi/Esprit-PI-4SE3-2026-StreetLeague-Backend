package tn.esprit.pi.gestiontournoi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.gestiontournoi.entity.GestionTournament;

public interface GestionTournamentRepository extends JpaRepository<GestionTournament, Long> {
}
