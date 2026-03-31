package tn.esprit.pi.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import tn.esprit.pi.domain.Role;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_Success() {
        User user = new User();
        user.setEmail("user@test.com");
        user.setPassword("hashed_pass");
        user.setRole(Role.PLAYER);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("user@test.com");
        assertEquals("user@test.com", result.getUsername());
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PLAYER")));
    }

    @Test
    void loadUserByUsername_NotFound_ThrowsException() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("unknown@test.com"));
    }
}