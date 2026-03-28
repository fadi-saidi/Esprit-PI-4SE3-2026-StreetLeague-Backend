package tn.esprit.pi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.Venue;
import tn.esprit.pi.domain.VenueOwnerProfile;
import tn.esprit.pi.dto.VenueDTO;
import tn.esprit.pi.repository.VenueOwnerProfileRepository;
import tn.esprit.pi.repository.VenueRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminVenueServiceImpl implements IAdminVenueService {

    private final VenueRepository venueRepository;
    private final VenueOwnerProfileRepository venueOwnerProfileRepository;

    private VenueDTO toVenueDTO(Venue v) {
        return VenueDTO.builder()
                .id(v.getId())
                .name(v.getName())
                .address(v.getAddress())
                .pricePerHour(v.getPricePerHour())
                .capacity(v.getCapacity())
                .sportType(v.getSportType())
                .build();
    }

    @Override
    public List<VenueDTO> getAllVenues() {
        return venueRepository.findAll().stream()
                .map(this::toVenueDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public VenueDTO createVenueForOwner(Long ownerId, VenueDTO dto) {
        VenueOwnerProfile owner = venueOwnerProfileRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Venue Owner not found: " + ownerId));

        Venue venue = new Venue();
        venue.setName(dto.getName());
        venue.setAddress(dto.getAddress());
        venue.setPricePerHour(dto.getPricePerHour());
        venue.setCapacity(dto.getCapacity());
        venue.setSportType(dto.getSportType());
        venue.setVenueOwnerProfile(owner);

        return toVenueDTO(venueRepository.save(venue));
    }

    @Override
    public VenueDTO updateVenueForOwner(Long venueId, Long ownerId, VenueDTO dto) {
        VenueOwnerProfile owner = venueOwnerProfileRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Venue Owner not found: " + ownerId));

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found: " + venueId));

        // Ensure the venue belongs to the specified owner
        if (venue.getVenueOwnerProfile() == null || !venue.getVenueOwnerProfile().getId().equals(ownerId)) {
            throw new RuntimeException("Venue does not belong to the specified owner.");
        }

        venue.setName(dto.getName());
        venue.setAddress(dto.getAddress());
        venue.setPricePerHour(dto.getPricePerHour());
        venue.setCapacity(dto.getCapacity());
        venue.setSportType(dto.getSportType());
        venue.setVenueOwnerProfile(owner);

        return toVenueDTO(venueRepository.save(venue));
    }

    @Override
    public void deleteVenueForOwner(Long venueId, Long ownerId) {
        VenueOwnerProfile owner = venueOwnerProfileRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Venue Owner not found: " + ownerId));

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found: " + venueId));

        // Ensure the venue belongs to the specified owner
        if (venue.getVenueOwnerProfile() == null || !venue.getVenueOwnerProfile().getId().equals(ownerId)) {
            throw new RuntimeException("Venue does not belong to the specified owner.");
        }

        venueRepository.delete(venue);
    }
}