package tn.esprit.pi.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.repository.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileHelperTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CoachProfileRepository coachProfileRepository;
    @Mock
    private PlayerProfileRepository playerProfileRepository;
    @Mock
    private RefereeProfileRepository refereeProfileRepository;
    @Mock
    private HealthProfessionalProfileRepository healthProfessionalProfileRepository;
    @Mock
    private SponsorProfileRepository sponsorProfileRepository;
    @Mock
    private VenueOwnerProfileRepository venueOwnerProfileRepository;
    @Mock
    private AdminProfileRepository adminProfileRepository;

    @InjectMocks
    private UserProfileHelper userProfileHelper;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setRole(Role.PLAYER);
    }

    @Test
    void getProfileByUser_ShouldReturnPlayerProfile() {
        // Given
        PlayerProfile profile = new PlayerProfile();
        when(playerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        // When
        Object result = userProfileHelper.getProfileByUser(user);

        // Then
        assertThat(result).isEqualTo(profile);
    }

    @Test
    void getProfileByUser_ShouldReturnCoachProfile() {
        // Given
        user.setRole(Role.COACH);
        CoachProfile profile = new CoachProfile();
        when(coachProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        // When
        Object result = userProfileHelper.getProfileByUser(user);

        // Then
        assertThat(result).isEqualTo(profile);
    }

    @Test
    void getProfileByUser_ShouldReturnNullWhenNotFound() {
        // Given
        when(playerProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // When
        Object result = userProfileHelper.getProfileByUser(user);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void getPlayerProfile_ShouldReturnProfile() {
        // Given
        PlayerProfile profile = new PlayerProfile();
        when(playerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        // When
        Optional<PlayerProfile> result = userProfileHelper.getPlayerProfile(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(profile);
    }

    @Test
    void hasRole_ShouldReturnTrue() {
        // When
        boolean result = userProfileHelper.hasRole(user, Role.PLAYER);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasRole_ShouldReturnFalse() {
        // When
        boolean result = userProfileHelper.hasRole(user, Role.COACH);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getUserByEmail_ShouldReturnUser() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userProfileHelper.getUserByEmail("test@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(user);
    }
}
