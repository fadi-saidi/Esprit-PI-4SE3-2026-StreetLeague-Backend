package tn.esprit.pi.gestiontournoi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.gestiontournoi.entity.ApprovalStatus;
import tn.esprit.pi.gestiontournoi.entity.GestionEvent;

import java.util.List;

public interface GestionEventRepository extends JpaRepository<GestionEvent, Long> {
    List<GestionEvent> findByApprovalStatusOrderByDateAscIdDesc(ApprovalStatus approvalStatus);

    List<GestionEvent> findAllByOrderByCreatedAtDesc();
}
