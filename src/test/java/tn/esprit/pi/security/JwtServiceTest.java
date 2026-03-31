package tn.esprit.pi.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    // Une clé de 64 caractères pour éviter les erreurs de taille de clé HS256
    private final String SECRET = "my_super_secret_key_for_testing_purposes_only_123456789012345678";

    @BeforeEach
    void setUp() {
        // Initialisation avec 1 heure d'expiration (3600000 ms)
        jwtService = new JwtService(SECRET, 3600000);
        userDetails = new User("test@esprit.tn", "password",
                List.of(new SimpleGrantedAuthority("ROLE_PLAYER")));
    }

    @Test
    void generateAndExtractEmail_Success() {
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
        String email = jwtService.extractEmail(token);
        assertEquals("test@esprit.tn", email);
    }

    @Test
    void isTokenValid_Success() {
        String token = jwtService.generateToken(userDetails);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_InvalidUser_ReturnsFalse() {
        String token = jwtService.generateToken(userDetails);
        UserDetails otherUser = new User("wrong@test.com", "pass", List.of());
        assertFalse(jwtService.isTokenValid(token, otherUser));
    }
}