package tn.esprit.pi.service;

import tn.esprit.pi.dto.VenueDTO;
import tn.esprit.pi.dto.VenueOwnerWithVenuesDTO;

import java.util.List;

public interface IAdminVenueService {

    // Voir tous les owners avec leurs venues
    List<VenueOwnerWithVenuesDTO> getAllOwnersWithVenues();

    // Voir un owner avec ses venues
    VenueOwnerWithVenuesDTO getOwnerWithVenues(Long ownerId);

    // Sur les venues : modifier et supprimer seulement
    VenueDTO updateVenue(Long venueId, VenueDTO dto);
    void deleteVenue(Long venueId);

    // Sur les owners : verifier / deverifier / supprimer
    VenueOwnerWithVenuesDTO verifyOwner(Long ownerId);
    VenueOwnerWithVenuesDTO unverifyOwner(Long ownerId);
    void deleteOwner(Long ownerId);
}