package tn.esprit.pi.gestiontournoi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.gestiontournoi.entity.GestionEvent;

public interface GestionEventRepository extends JpaRepository<GestionEvent, Long> {
}
