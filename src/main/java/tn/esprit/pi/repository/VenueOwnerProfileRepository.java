package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi.domain.VenueOwnerProfile;

import java.util.Optional;

@Repository
public interface VenueOwnerProfileRepository extends JpaRepository<VenueOwnerProfile, Long> {

    // Utilise par UserProfileHelper
    Optional<VenueOwnerProfile> findByUserId(Long userId);
}