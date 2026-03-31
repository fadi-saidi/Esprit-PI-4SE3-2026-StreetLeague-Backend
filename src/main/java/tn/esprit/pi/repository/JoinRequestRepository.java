package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.domain.JoinRequest;
import tn.esprit.pi.domain.Team;
import tn.esprit.pi.domain.User;

import java.util.List;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    List<JoinRequest> findByTeamAndStatusAndType(Team team, String status, String type);
    List<JoinRequest> findByPlayerAndStatusAndType(User player, String status, String type);
    boolean existsByTeamAndPlayerAndStatusAndType(Team team, User player, String status, String type);
    void deleteByTeam(Team team);
}
