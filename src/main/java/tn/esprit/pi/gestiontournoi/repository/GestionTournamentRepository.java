package tn.esprit.pi.gestiontournoi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.gestiontournoi.entity.ApprovalStatus;
import tn.esprit.pi.gestiontournoi.entity.GestionTournament;

import java.util.List;

public interface GestionTournamentRepository extends JpaRepository<GestionTournament, Long> {
    List<GestionTournament> findByApprovalStatusOrderByStartDateAscIdDesc(ApprovalStatus approvalStatus);

    List<GestionTournament> findAllByOrderByCreatedAtDesc();
}
