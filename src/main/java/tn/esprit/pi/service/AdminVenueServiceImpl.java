package tn.esprit.pi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.Venue;
import tn.esprit.pi.domain.VenueOwnerProfile;
import tn.esprit.pi.dto.VenueDTO;
import tn.esprit.pi.dto.VenueOwnerWithVenuesDTO;
import tn.esprit.pi.repository.VenueOwnerProfileRepository;
import tn.esprit.pi.repository.VenueRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminVenueServiceImpl implements IAdminVenueService {

    private final VenueRepository venueRepository;
    private final VenueOwnerProfileRepository venueOwnerProfileRepository;

    // ─── Mappers ──────────────────────────────────────────────────────────────

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

    private VenueOwnerWithVenuesDTO toOwnerWithVenuesDTO(VenueOwnerProfile owner) {
        List<VenueDTO> venues = owner.getVenues() != null
                ? owner.getVenues().stream().map(this::toVenueDTO).collect(Collectors.toList())
                : List.of();

        return VenueOwnerWithVenuesDTO.builder()
                .ownerId(owner.getId())
                .ownerEmail(owner.getUser().getEmail())
                .ownerUsername(owner.getUser().getUsername())
                .ownerPhone(owner.getPhone())
                .companyName(owner.getCompanyName())
                .verified(owner.getVerified())
                .venues(venues)
                .build();
    }

    // ─── Owners avec leurs Venues ─────────────────────────────────────────────

    @Override
    public List<VenueOwnerWithVenuesDTO> getAllOwnersWithVenues() {
        return venueOwnerProfileRepository.findAll()
                .stream()
                .map(this::toOwnerWithVenuesDTO)
                .collect(Collectors.toList());
    }

    @Override
    public VenueOwnerWithVenuesDTO getOwnerWithVenues(Long ownerId) {
        VenueOwnerProfile owner = venueOwnerProfileRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found: " + ownerId));
        return toOwnerWithVenuesDTO(owner);
    }

    // ─── Actions sur les Venues (pas d'ajout) ────────────────────────────────

    @Override
    public VenueDTO updateVenue(Long venueId, VenueDTO dto) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found: " + venueId));

        venue.setName(dto.getName());
        venue.setAddress(dto.getAddress());
        venue.setPricePerHour(dto.getPricePerHour());
        venue.setCapacity(dto.getCapacity());
        venue.setSportType(dto.getSportType());

        return toVenueDTO(venueRepository.save(venue));
    }

    @Override
    public void deleteVenue(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found: " + venueId));
        venueRepository.delete(venue);
    }

    // ─── Actions sur les Owners ───────────────────────────────────────────────

    @Override
    public VenueOwnerWithVenuesDTO verifyOwner(Long ownerId) {
        VenueOwnerProfile owner = venueOwnerProfileRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found: " + ownerId));
        owner.setVerified(true);
        return toOwnerWithVenuesDTO(venueOwnerProfileRepository.save(owner));
    }

    @Override
    public VenueOwnerWithVenuesDTO unverifyOwner(Long ownerId) {
        VenueOwnerProfile owner = venueOwnerProfileRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found: " + ownerId));
        owner.setVerified(false);
        return toOwnerWithVenuesDTO(venueOwnerProfileRepository.save(owner));
    }

    @Override
    public void deleteOwner(Long ownerId) {
        VenueOwnerProfile owner = venueOwnerProfileRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found: " + ownerId));
        venueOwnerProfileRepository.delete(owner);
    }
}