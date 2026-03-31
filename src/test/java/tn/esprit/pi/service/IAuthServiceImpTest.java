package tn.esprit.pi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.Dtos.*;
import tn.esprit.pi.repository.*;
import tn.esprit.pi.security.CustomUserDetailsService;
import tn.esprit.pi.security.JwtService;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Objectif 90% - AuthService Coverage")
class IAuthServiceImpTest {

    @Mock private UserRepository userRepository;
    @Mock private PlayerProfileRepository playerProfileRepository;
    @Mock private CoachProfileRepository coachProfileRepository;
    @Mock private RefereeProfileRepository refereeProfileRepository;
    @Mock private HealthProfessionalProfileRepository healthProfessionalProfileRepository;
    @Mock private SponsorProfileRepository sponsorProfileRepository;
    @Mock private VenueOwnerProfileRepository venueOwnerProfileRepository;
    @Mock private AdminProfileRepository adminProfileRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private JwtService jwtService;

    @InjectMocks private IAuthServiceImp authService;

    private RegisterRequest baseRequest;

    @BeforeEach
    void setUp() {
        baseRequest = new RegisterRequest(
                "Full Name", "test@test.com", "password123", Role.PLAYER,
                "2000-01-01", null, null, null, 0, null, null, null, 0.0, "12345678"
        );
    }

    @Test
    @DisplayName("Register - Erreur si l'email existe déjà")
    void register_EmailExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));
        assertThrows(IllegalArgumentException.class, () -> authService.register(baseRequest));
    }

    @Test
    @DisplayName("Register - Cas REFEREE")
    void register_Referee_Success() {
        RegisterRequest req = new RegisterRequest(
                "Referee", "ref@test.com", "pass123", Role.REFEREE,
                null, "Cert", "LIC123", null, 10, null, null, null, 0.0, null
        );
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("enc");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        authService.register(req);
        verify(refereeProfileRepository).save(any(RefereeProfile.class));
    }

    @Test
    @DisplayName("Register - Cas ADMIN")
    void register_Admin_Success() {
        RegisterRequest req = new RegisterRequest(
                "Admin", "admin@test.com", "pass123", Role.ADMIN,
                null, null, null, null, 0, null, null, null, 0.0, null
        );
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        authService.register(req);
        verify(adminProfileRepository).save(any(AdminProfile.class));
    }

    @Test
    @DisplayName("Login - Succès avec génération de Token")
    void login_Success() {
        LoginRequest loginReq = new LoginRequest("test@test.com", "password123");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setRole(Role.PLAYER);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@test.com");
        // Utilisation de doReturn pour éviter les problèmes de types génériques avec les authorities
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_PLAYER")))
                .when(userDetails).getAuthorities();

        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("fake-jwt-token");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(playerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(new PlayerProfile()));

        AuthResponse response = authService.login(loginReq);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.token());
        assertEquals("ROLE_PLAYER", response.role());
    }
}