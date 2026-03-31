package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.domain.OwnedPlayer;
import tn.esprit.pi.domain.PlayerStatus;

import java.util.List;
import java.util.Optional;

public interface OwnedPlayerRepository extends JpaRepository<OwnedPlayer, Long> {

    // Find all players of a specific virtual team
    List<OwnedPlayer> findByVirtualTeamId(Long virtualTeamId);

    // Find all players owned by a specific user
    List<OwnedPlayer> findByUserId(Long userId);

    // Check if a user already owns a player in a team
    Optional<OwnedPlayer> findByUserIdAndVirtualTeamId(Long userId, Long virtualTeamId);
    long countByVirtualTeamIdAndStatus(Long virtualTeamId, PlayerStatus status);
}