package tn.esprit.pi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.Dtos.AuthResponse;
import tn.esprit.pi.dto.Dtos.LoginRequest;
import tn.esprit.pi.dto.Dtos.RegisterRequest;
import tn.esprit.pi.repository.*;
import tn.esprit.pi.security.CustomUserDetailsService;
import tn.esprit.pi.security.jwt.JwtService;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IAuthServiceImpTest {

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

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private IAuthServiceImp authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "Test User",
                "test@example.com",
                "password123",
                Role.PLAYER,
                "1990-01-01",
                "123456789",
                null, null, null, null, null, null, null, null
        );

        loginRequest = new LoginRequest("test@example.com", "password123");

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("Test User");
        user.setRole(Role.PLAYER);
    }

    @Test
    void register_ShouldCreatePlayerProfile() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(playerProfileRepository.save(any(PlayerProfile.class))).thenReturn(new PlayerProfile());

        // Act
        User result = authService.register(registerRequest);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(playerProfileRepository).save(any(PlayerProfile.class));
    }

    @Test
    void register_ShouldThrowExceptionIfEmailExists() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.register(registerRequest));
        assertEquals("Email already used", exception.getMessage());
    }

    @Test
    void register_ShouldThrowExceptionIfPasswordTooShort() {
        // Arrange
        RegisterRequest badRequest = new RegisterRequest(
                "Test User",
                "test@example.com",
                "123",
                Role.PLAYER,
                null, null, null, null, null, null, null, null, null, null
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.register(badRequest));
        assertEquals("Password must contain at least 6 characters", exception.getMessage());
    }

    @Test
    void login_ShouldReturnAuthResponse() {
        // Arrange
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("Test User");
        when(userDetails.getAuthorities()).thenReturn((java.util.Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_PLAYER")));

        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwtToken");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(playerProfileRepository.findByUserId(anyLong())).thenReturn(Optional.of(new PlayerProfile()));

        // Act
        AuthResponse result = authService.login(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals("jwtToken", result.token());
        assertEquals("Test User", result.email());
        assertEquals("ROLE_PLAYER", result.role());
    }
}
