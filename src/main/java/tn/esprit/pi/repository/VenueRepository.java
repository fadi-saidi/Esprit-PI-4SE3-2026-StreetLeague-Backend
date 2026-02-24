package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi.domain.Venue;

import java.util.List;
import java.util.Optional;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    // Récupérer toutes les venues d'un VenueOwner
    List<Venue> findByVenueOwnerProfile_Id(Long venueOwnerId);

    // Vérifier si la venue appartient bien au VenueOwner (sécurité)
    Optional<Venue> findByIdAndVenueOwnerProfile_Id(Long venueId, Long venueOwnerId);
}