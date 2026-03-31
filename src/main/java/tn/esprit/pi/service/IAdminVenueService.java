package tn.esprit.pi.service;

import tn.esprit.pi.dto.VenueDTO;

import java.util.List;

public interface IAdminVenueService {
    List<VenueDTO> getAllVenues();
    VenueDTO createVenueForOwner(Long ownerId, VenueDTO dto);
    VenueDTO updateVenueForOwner(Long venueId, Long ownerId, VenueDTO dto);
    void deleteVenueForOwner(Long venueId, Long ownerId);
}