package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.domain.PlayerProfile;

import java.util.List;
import java.util.Optional;

public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, Long> {

    // Find all players by level
    List<PlayerProfile> findByLevel(tn.esprit.pi.domain.PlayerLevel level);

    // Find all players belonging to a team
    List<PlayerProfile> findByTeams_Id(Long teamId);

    // Find all players participating in a training
    List<PlayerProfile> findByTrainings_Id(Long trainingId);

    // Find a player profile by user ID
    Optional<PlayerProfile> findByUserId(Long userId);
}