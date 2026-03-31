package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pi.domain.PlayerProfile;
import tn.esprit.pi.domain.SportType;

import java.util.List;
import java.util.Optional;

public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, Long> {

    List<PlayerProfile> findByLevel(tn.esprit.pi.domain.PlayerLevel level);
    List<PlayerProfile> findByTeams_Id(Long teamId);
    List<PlayerProfile> findByTrainings_Id(Long trainingId);
    Optional<PlayerProfile> findByUserId(Long userId);

    @Query("SELECT p FROM PlayerProfile p WHERE p.user.email = :email")
    Optional<PlayerProfile> findByUserEmail(@Param("email") String email);

    List<PlayerProfile> findBySportType(SportType sportType);
}
