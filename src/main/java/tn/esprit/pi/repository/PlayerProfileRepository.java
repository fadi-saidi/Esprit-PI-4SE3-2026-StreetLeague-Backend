package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pi.domain.PlayerProfile;

import java.util.Optional;

public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, Long> {
    Optional<PlayerProfile> findByUserId(Long userId);

    @Query("SELECT p FROM PlayerProfile p WHERE p.user.email = :email")
    Optional<PlayerProfile> findByUserEmail(@Param("email") String email);
}
