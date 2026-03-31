package tn.esprit.pi.service;

import tn.esprit.pi.dto.VenueOwnerWithVenuesDTO;
import java.util.List;

public interface IAdminVenueOwnerService {
    List<VenueOwnerWithVenuesDTO> getAllOwnersWithVenues();
    VenueOwnerWithVenuesDTO getOwnerWithVenues(Long ownerId);
    VenueOwnerWithVenuesDTO verifyOwner(Long ownerId);
    VenueOwnerWithVenuesDTO unverifyOwner(Long ownerId);
    void deleteOwner(Long ownerId);
}
