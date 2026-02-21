package tn.esprit.pi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.AppUser;
import tn.esprit.pi.dto.Dtos.AuthResponse;
import tn.esprit.pi.dto.Dtos.LoginRequest;
import tn.esprit.pi.dto.Dtos.RegisterRequest;
import tn.esprit.pi.repository.UserRepository;
import tn.esprit.pi.security.CustomUserDetailsService;
import tn.esprit.pi.security.jwt.JwtService;

@Service
@RequiredArgsConstructor
public class IAuthServiceImp implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    // ─── Register ─────────────────────────────────────────────────────────────

    @Override
    public AppUser register(RegisterRequest req) {
        // Validate email
        if (req.email() == null || req.email().isBlank()) {
            throw new IllegalArgumentException("Email required");
        }
        // Check if email already used
        if (userRepository.findByEmail(req.email()).isPresent()) {
            throw new IllegalArgumentException("Email already used");
        }
        // Validate password
        if (req.password() == null || req.password().length() < 6) {
            throw new IllegalArgumentException("Password must contain at least 6 characters");
        }
        // Validate role
        if (req.role() == null) {
            throw new IllegalArgumentException("Role required");
        }

        AppUser u = new AppUser();
        u.setUsername(req.fullName() == null ? "Not Available" : req.fullName());
        u.setEmail(req.email());
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setRole(req.role());
        u.setEnabled(true);

        return userRepository.save(u);
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    @Override
    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(req.email());

        String token = jwtService.generateToken(userDetails);

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No roles found"))
                .getAuthority();

        return new AuthResponse(token, userDetails.getUsername(), role);
    }
}
