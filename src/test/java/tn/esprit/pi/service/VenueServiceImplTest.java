package tn.esprit.pi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.VenueDTO;
import tn.esprit.pi.repository.UserRepository;
import tn.esprit.pi.repository.VenueOwnerProfileRepository;
import tn.esprit.pi.repository.VenueRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VenueServiceImplTest {

    @Mock private VenueRepository              venueRepository;
    @Mock private VenueOwnerProfileRepository  venueOwnerProfileRepository;
    @Mock private UserRepository               userRepository;

    @InjectMocks private VenueServiceImpl venueService;

    private User             owner;
    private VenueOwnerProfile ownerProfile;
    private Venue             venue;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setUsername("alice");
        owner.setEmail("alice@test.com");

        ownerProfile = new VenueOwnerProfile();
        ownerProfile.setId(1L);
        ownerProfile.setUser(owner);

        venue = new Venue();
        venue.setId(10L);
        venue.setName("Arena Nord");
        venue.setAddress("Tunis");
        venue.setPricePerHour(50.0);
        venue.setCapacity(22);
        venue.setSportType(SportType.FOOTBALL);
        venue.setAvailable(true);
        venue.setVerified(false);
        venue.setVenueOwnerProfile(ownerProfile);
    }

    // ── createVenue ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("createVenue: saves venue linked to owner profile")
    void createVenue_success() {
        VenueDTO dto = VenueDTO.builder()
                .name("Arena Nord").address("Tunis")
                .pricePerHour(50.0).capacity(22)
                .sportType(SportType.FOOTBALL).build();

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(owner));
        when(venueOwnerProfileRepository.findById(1L)).thenReturn(Optional.of(ownerProfile));
        when(venueRepository.save(any())).thenReturn(venue);

        VenueDTO result = venueService.createVenue(dto, "alice@test.com");

        assertThat(result.getName()).isEqualTo("Arena Nord");
        assertThat(result.getPricePerHour()).isEqualTo(50.0);
        assertThat(result.getSportType()).isEqualTo(SportType.FOOTBALL);
        verify(venueRepository).save(any(Venue.class));
    }

    @Test
    @DisplayName("createVenue: unknown owner email throws RuntimeException")
    void createVenue_unknownEmail() {
        VenueDTO dto = VenueDTO.builder().name("X").address("Y")
                .pricePerHour(10.0).capacity(5).sportType(SportType.TENNIS).build();

        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> venueService.createVenue(dto, "ghost@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("createVenue: user without VenueOwnerProfile throws RuntimeException")
    void createVenue_noProfile() {
        VenueDTO dto = VenueDTO.builder().name("X").address("Y")
                .pricePerHour(10.0).capacity(5).sportType(SportType.TENNIS).build();

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(owner));
        when(venueOwnerProfileRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> venueService.createVenue(dto, "alice@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("VenueOwnerProfile not found");
    }

    // ── getMyVenues ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getMyVenues: returns venues belonging to owner")
    void getMyVenues_success() {
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(owner));
        when(venueRepository.findByVenueOwnerProfile_Id(1L)).thenReturn(List.of(venue));

        List<VenueDTO> result = venueService.getMyVenues("alice@test.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Arena Nord");
    }

    @Test
    @DisplayName("getMyVenues: owner with no venues returns empty list")
    void getMyVenues_empty() {
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(owner));
        when(venueRepository.findByVenueOwnerProfile_Id(1L)).thenReturn(List.of());

        List<VenueDTO> result = venueService.getMyVenues("alice@test.com");

        assertThat(result).isEmpty();
    }

    // ── getAllVenues ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllVenues: returns all venues")
    void getAllVenues_success() {
        when(venueRepository.findAll()).thenReturn(List.of(venue));

        List<VenueDTO> result = venueService.getAllVenues();

        assertThat(result).hasSize(1);
    }

    // ── updateVenue ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateVenue: updates all fields and returns updated DTO")
    void updateVenue_success() {
        VenueDTO dto = VenueDTO.builder()
                .name("Arena Sud").address("Sousse")
                .pricePerHour(80.0).capacity(30)
                .sportType(SportType.BASKETBALL).build();

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(owner));
        when(venueRepository.findByIdAndVenueOwnerProfile_Id(10L, 1L)).thenReturn(Optional.of(venue));
        when(venueRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VenueDTO result = venueService.updateVenue(10L, dto, "alice@test.com");

        assertThat(result.getName()).isEqualTo("Arena Sud");
        assertThat(result.getPricePerHour()).isEqualTo(80.0);
        assertThat(result.getSportType()).isEqualTo(SportType.BASKETBALL);
    }

    @Test
    @DisplayName("updateVenue: venue not owned throws RuntimeException")
    void updateVenue_accessDenied() {
        VenueDTO dto = VenueDTO.builder().name("X").address("Y")
                .pricePerHour(10.0).capacity(5).sportType(SportType.TENNIS).build();

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(owner));
        when(venueRepository.findByIdAndVenueOwnerProfile_Id(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> venueService.updateVenue(10L, dto, "alice@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("access denied");
    }

    // ── deleteVenue ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteVenue: deletes venue owned by user")
    void deleteVenue_success() {
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(owner));
        when(venueRepository.findByIdAndVenueOwnerProfile_Id(10L, 1L)).thenReturn(Optional.of(venue));

        venueService.deleteVenue(10L, "alice@test.com");

        verify(venueRepository).delete(venue);
    }

    // ── toggleAvailability ────────────────────────────────────────────────────

    @Test
    @DisplayName("toggleAvailability: flips available flag from true to false")
    void toggleAvailability_trueToFalse() {
        venue.setAvailable(true);
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(owner));
        when(venueRepository.findByIdAndVenueOwnerProfile_Id(10L, 1L)).thenReturn(Optional.of(venue));
        when(venueRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VenueDTO result = venueService.toggleAvailability(10L, "alice@test.com");

        assertThat(result.getAvailable()).isFalse();
    }

    @Test
    @DisplayName("toggleAvailability: flips available flag from false to true")
    void toggleAvailability_falseToTrue() {
        venue.setAvailable(false);
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(owner));
        when(venueRepository.findByIdAndVenueOwnerProfile_Id(10L, 1L)).thenReturn(Optional.of(venue));
        when(venueRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VenueDTO result = venueService.toggleAvailability(10L, "alice@test.com");

        assertThat(result.getAvailable()).isTrue();
    }

    // ── updatePhotoUrl ────────────────────────────────────────────────────────

    @Test
    @DisplayName("updatePhotoUrl: saves new photo URL")
    void updatePhotoUrl_success() {
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(owner));
        when(venueRepository.findByIdAndVenueOwnerProfile_Id(10L, 1L)).thenReturn(Optional.of(venue));
        when(venueRepository.save(any())).thenReturn(venue);

        venueService.updatePhotoUrl(10L, "/photos/arena.jpg", "alice@test.com");

        assertThat(venue.getPhotoUrl()).isEqualTo("/photos/arena.jpg");
    }
}
