package tn.esprit.pi.gestiontournoi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.gestiontournoi.entity.GestionMatch;

public interface GestionMatchRepository extends JpaRepository<GestionMatch, Long> {
}
