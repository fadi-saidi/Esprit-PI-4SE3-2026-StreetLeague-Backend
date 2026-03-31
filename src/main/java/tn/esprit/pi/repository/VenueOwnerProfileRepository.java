package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pi.domain.VenueOwnerProfile;

import java.util.Optional;

public interface VenueOwnerProfileRepository extends JpaRepository<VenueOwnerProfile, Long> {
    Optional<VenueOwnerProfile> findByUserId(Long userId);

    @Query("SELECT v FROM VenueOwnerProfile v WHERE v.user.email = :email")
    Optional<VenueOwnerProfile> findByUserEmail(@Param("email") String email);
}
