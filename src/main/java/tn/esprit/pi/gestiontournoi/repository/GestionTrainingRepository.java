package tn.esprit.pi.gestiontournoi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.gestiontournoi.entity.GestionTraining;

public interface GestionTrainingRepository extends JpaRepository<GestionTraining, Long> {
}
