package tn.esprit.pi.gestiontournoi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.gestiontournoi.entity.ApprovalStatus;
import tn.esprit.pi.gestiontournoi.entity.GestionMatch;

import java.util.List;

public interface GestionMatchRepository extends JpaRepository<GestionMatch, Long> {
    List<GestionMatch> findByApprovalStatusOrderByDateAscIdDesc(ApprovalStatus approvalStatus);

    List<GestionMatch> findAllByOrderByCreatedAtDesc();
}
