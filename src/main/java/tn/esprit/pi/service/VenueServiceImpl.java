package tn.esprit.pi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.domain.Venue;
import tn.esprit.pi.domain.VenueOwnerProfile;
import tn.esprit.pi.dto.VenueDTO;
import tn.esprit.pi.repository.UserRepository;
import tn.esprit.pi.repository.VenueOwnerProfileRepository;
import tn.esprit.pi.repository.VenueRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements IVenueService {

    private final VenueRepository venueRepository;
    private final VenueOwnerProfileRepository venueOwnerProfileRepository;
    private final UserRepository userRepository;

    // ─── Mapper ───────────────────────────────────────────────────────────────

    private VenueDTO toDTO(Venue venue) {
        return VenueDTO.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .pricePerHour(venue.getPricePerHour())
                .capacity(venue.getCapacity())
                .sportType(venue.getSportType())
                .build();
    }

    // ─── Helper : recuperer l'ID du owner via son email ──────────────────────

    private Long getOwnerIdByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        return user.getId(); // Meme ID que VenueOwnerProfile grace a @MapsId
    }

    private VenueOwnerProfile getOwnerProfile(String email) {
        Long ownerId = getOwnerIdByEmail(email);
        return venueOwnerProfileRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("VenueOwnerProfile not found for: " + email));
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    @Override
    public VenueDTO createVenue(VenueDTO dto, String email) {
        VenueOwnerProfile owner = getOwnerProfile(email);

        Venue venue = new Venue();
        venue.setName(dto.getName());
        venue.setAddress(dto.getAddress());
        venue.setPricePerHour(dto.getPricePerHour());
        venue.setCapacity(dto.getCapacity());
        venue.setSportType(dto.getSportType());
        venue.setVenueOwnerProfile(owner);

        return toDTO(venueRepository.save(venue));
    }

    @Override
    public List<VenueDTO> getMyVenues(String email) {
        Long ownerId = getOwnerIdByEmail(email);
        return venueRepository.findByVenueOwnerProfile_Id(ownerId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public VenueDTO getVenueById(Long venueId, String email) {
        Long ownerId = getOwnerIdByEmail(email);
        Venue venue = venueRepository.findByIdAndVenueOwnerProfile_Id(venueId, ownerId)
                .orElseThrow(() -> new RuntimeException("Venue not found or access denied"));
        return toDTO(venue);
    }

    @Override
    public VenueDTO updateVenue(Long venueId, VenueDTO dto, String email) {
        Long ownerId = getOwnerIdByEmail(email);
        Venue venue = venueRepository.findByIdAndVenueOwnerProfile_Id(venueId, ownerId)
                .orElseThrow(() -> new RuntimeException("Venue not found or access denied"));

        venue.setName(dto.getName());
        venue.setAddress(dto.getAddress());
        venue.setPricePerHour(dto.getPricePerHour());
        venue.setCapacity(dto.getCapacity());
        venue.setSportType(dto.getSportType());

        return toDTO(venueRepository.save(venue));
    }

    @Override
    public void deleteVenue(Long venueId, String email) {
        Long ownerId = getOwnerIdByEmail(email);
        Venue venue = venueRepository.findByIdAndVenueOwnerProfile_Id(venueId, ownerId)
                .orElseThrow(() -> new RuntimeException("Venue not found or access denied"));
        venueRepository.delete(venue);
    }
}