package tn.esprit.pi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi.domain.VenueOwnerProfile;
import tn.esprit.pi.dto.VenueDTO;
import tn.esprit.pi.dto.VenueOwnerWithVenuesDTO;
import tn.esprit.pi.repository.VenueOwnerProfileRepository;
import tn.esprit.pi.repository.VenueRepository;
import tn.esprit.pi.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminVenueOwnerServiceImpl implements IAdminVenueOwnerService {

    private final VenueOwnerProfileRepository venueOwnerProfileRepository;
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;

    private VenueDTO toVenueDTO(tn.esprit.pi.domain.Venue v) {
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
                .orElseThrow(() -> new RuntimeException("Venue Owner not found: " + ownerId));
        return toOwnerWithVenuesDTO(owner);
    }

    @Override
    public VenueOwnerWithVenuesDTO verifyOwner(Long ownerId) {
        VenueOwnerProfile owner = venueOwnerProfileRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Venue Owner not found: " + ownerId));
        owner.setVerified(true);
        return toOwnerWithVenuesDTO(venueOwnerProfileRepository.save(owner));
    }

    @Override
    public VenueOwnerWithVenuesDTO unverifyOwner(Long ownerId) {
        VenueOwnerProfile owner = venueOwnerProfileRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Venue Owner not found: " + ownerId));
        owner.setVerified(false);
        return toOwnerWithVenuesDTO(venueOwnerProfileRepository.save(owner));
    }

    @Override
    @Transactional
    public void deleteOwner(Long ownerId) {
        VenueOwnerProfile owner = venueOwnerProfileRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Venue Owner not found: " + ownerId));

        // 1. Supprimer tous les Venues liés explicitement (avec leurs Reservations/Sponsorships en cascade)
        if (owner.getVenues() != null && !owner.getVenues().isEmpty()) {
            venueRepository.deleteAll(owner.getVenues());
            venueRepository.flush();
            owner.getVenues().clear();
        }

        // 2. Dissocier le profil de l'User pour éviter la contrainte FK @MapsId
        var user = owner.getUser();
        if (user != null) {
            user.setVenueOwnerProfile(null);
            userRepository.save(user);
        }

        // 3. Supprimer le profil VenueOwnerProfile
        venueOwnerProfileRepository.delete(owner);
        venueOwnerProfileRepository.flush();
    }
}
