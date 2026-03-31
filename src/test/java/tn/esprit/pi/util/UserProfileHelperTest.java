package tn.esprit.pi.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.repository.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Utils - UserProfileHelper (Full Coverage)")
class UserProfileHelperTest {

    @Mock private UserRepository userRepository;
    @Mock private CoachProfileRepository coachProfileRepository;
    @Mock private PlayerProfileRepository playerProfileRepository;
    @Mock private RefereeProfileRepository refereeProfileRepository;
    @Mock private HealthProfessionalProfileRepository healthProfessionalProfileRepository;
    @Mock private SponsorProfileRepository sponsorProfileRepository;
    @Mock private VenueOwnerProfileRepository venueOwnerProfileRepository;
    @Mock private AdminProfileRepository adminProfileRepository;

    @InjectMocks
    private UserProfileHelper userProfileHelper;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@esprit.tn");
    }

    @Test
    @DisplayName("getProfileByUser - Test de tous les rôles (Switch Coverage)")
    void getProfileByUser_AllRoles() {
        // COACH
        user.setRole(Role.COACH);
        when(coachProfileRepository.findByUserId(1L)).thenReturn(Optional.of(new CoachProfile()));
        assertNotNull(userProfileHelper.getProfileByUser(user));

        // PLAYER
        user.setRole(Role.PLAYER);
        when(playerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(new PlayerProfile()));
        assertNotNull(userProfileHelper.getProfileByUser(user));

        // REFEREE
        user.setRole(Role.REFEREE);
        when(refereeProfileRepository.findByUserId(1L)).thenReturn(Optional.of(new RefereeProfile()));
        assertNotNull(userProfileHelper.getProfileByUser(user));

        // HEALTH_PROFESSIONAL
        user.setRole(Role.HEALTH_PROFESSIONAL);
        when(healthProfessionalProfileRepository.findByUserId(1L)).thenReturn(Optional.of(new HealthProfessionalProfile()));
        assertNotNull(userProfileHelper.getProfileByUser(user));

        // SPONSOR
        user.setRole(Role.SPONSOR);
        when(sponsorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(new SponsorProfile()));
        assertNotNull(userProfileHelper.getProfileByUser(user));

        // VENUE_OWNER
        user.setRole(Role.VENUE_OWNER);
        when(venueOwnerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(new VenueOwnerProfile()));
        assertNotNull(userProfileHelper.getProfileByUser(user));

        // ADMIN
        user.setRole(Role.ADMIN);
        when(adminProfileRepository.findByUserId(1L)).thenReturn(Optional.of(new AdminProfile()));
        assertNotNull(userProfileHelper.getProfileByUser(user));
    }

    @Test
    @DisplayName("Méthodes Helper Individuelles")
    void individualHelpers_Test() {
        when(coachProfileRepository.findByUserId(1L)).thenReturn(Optional.of(new CoachProfile()));
        assertTrue(userProfileHelper.getCoachProfile(1L).isPresent());

        when(playerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(new PlayerProfile()));
        assertTrue(userProfileHelper.getPlayerProfile(1L).isPresent());

        when(refereeProfileRepository.findByUserId(1L)).thenReturn(Optional.of(new RefereeProfile()));
        assertTrue(userProfileHelper.getRefereeProfile(1L).isPresent());
    }

    @Test
    @DisplayName("hasRole et getUserByEmail")
    void basicHelpers_Test() {
        user.setRole(Role.ADMIN);
        assertTrue(userProfileHelper.hasRole(user, Role.ADMIN));

        when(userRepository.findByEmail("test@esprit.tn")).thenReturn(Optional.of(user));
        assertTrue(userProfileHelper.getUserByEmail("test@esprit.tn").isPresent());
    }
}