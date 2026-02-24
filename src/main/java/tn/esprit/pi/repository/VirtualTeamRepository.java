package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.domain.VirtualTeam;

import java.util.List;

public interface VirtualTeamRepository extends JpaRepository<VirtualTeam, Long> {

    List<VirtualTeam> findByUserId(Long userId);
}