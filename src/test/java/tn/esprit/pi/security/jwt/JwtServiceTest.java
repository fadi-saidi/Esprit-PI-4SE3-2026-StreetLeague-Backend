package tn.esprit.pi.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import tn.esprit.pi.security.JwtService;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("mySecretKeyForTestingPurposesOnly12345678901234567890", 3600000L); // 1 hour
    }

    @Test
    void generateToken_ShouldReturnToken() {
        // Given
        UserDetails userDetails = new User("test@example.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PLAYER")));

        // When
        String token = jwtService.generateToken(userDetails);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void extractEmail_ShouldReturnEmail() {
        // Given
        UserDetails userDetails = new User("test@example.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PLAYER")));
        String token = jwtService.generateToken(userDetails);

        // When
        String email = jwtService.extractEmail(token);

        // Then
        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    void isTokenValid_ShouldReturnTrue() {
        // Given
        UserDetails userDetails = new User("test@example.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PLAYER")));
        String token = jwtService.generateToken(userDetails);

        // When
        boolean valid = jwtService.isTokenValid(token, userDetails);

        // Then
        assertThat(valid).isTrue();
    }

    @Test
    void isTokenValid_ShouldReturnFalseForWrongUser() {
        // Given
        UserDetails userDetails1 = new User("test@example.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PLAYER")));
        String token = jwtService.generateToken(userDetails1);

        UserDetails userDetails2 = new User("other@example.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PLAYER")));

        // When
        boolean valid = jwtService.isTokenValid(token, userDetails2);

        // Then
        assertThat(valid).isFalse();
    }

    @Test
    void isTokenValid_ShouldReturnFalseForInvalidToken() {
        // Given
        UserDetails userDetails = new User("test@example.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_PLAYER")));

        // When
        boolean valid = jwtService.isTokenValid("invalid.token.here", userDetails);

        // Then
        assertThat(valid).isFalse();
    }
}
