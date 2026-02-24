package tn.esprit.pi.service;

import tn.esprit.pi.dto.VenueDTO;

import java.util.List;

public interface IVenueService {

    VenueDTO createVenue(VenueDTO dto, String email);

    List<VenueDTO> getMyVenues(String email);

    VenueDTO getVenueById(Long venueId, String email);

    VenueDTO updateVenue(Long venueId, VenueDTO dto, String email);

    void deleteVenue(Long venueId, String email);
}