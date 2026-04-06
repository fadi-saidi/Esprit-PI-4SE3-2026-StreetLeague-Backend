package tn.esprit.pi.gestiontournoi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.gestiontournoi.entity.ApprovalStatus;
import tn.esprit.pi.gestiontournoi.entity.GestionTraining;

import java.util.List;

public interface GestionTrainingRepository extends JpaRepository<GestionTraining, Long> {
    List<GestionTraining> findByApprovalStatusOrderByDateAscIdDesc(ApprovalStatus approvalStatus);

    List<GestionTraining> findAllByOrderByCreatedAtDesc();
}
