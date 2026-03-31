package tn.esprit.pi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.esprit.pi.domain.Role;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.dto.Dtos.AuthResponse;
import tn.esprit.pi.dto.Dtos.LoginRequest;
import tn.esprit.pi.dto.Dtos.RegisterRequest;
import tn.esprit.pi.service.IAuthService;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests - AuthController")
class AuthControllerTest {

    @Mock private IAuthService authService;
    @Mock private Authentication authentication;
    @InjectMocks private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    @DisplayName("Register - Succès")
    void register_Success() throws Exception {
        // Correction ici : On remplit le record avec les 14 paramètres (null ou valeurs par défaut pour le test)
        RegisterRequest req = new RegisterRequest(
                "test@test.com", "password", "testuser", Role.PLAYER,
                null, null, null, null, null, null, null, null, null, null
        );

        User mockUser = new User();
        mockUser.setEmail("test@test.com");

        // Dans votre controller : var saved = authService.register(req);
        // L'erreur indiquait qu'il attend un User en retour
        when(authService.register(any(RegisterRequest.class))).thenReturn(mockUser);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\",\"username\":\"testuser\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("User created: test@test.com")));
    }

    @Test
    @DisplayName("Login - Succès")
    void login_Success() throws Exception {
        // Correction : AuthResponse attend 4 paramètres (String, String, String, Long)
        AuthResponse res = new AuthResponse("token-secret", "test@test.com", "PLAYER", 1L);

        when(authService.login(any(LoginRequest.class))).thenReturn(res);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-secret"));
    }

    @Test
    @DisplayName("Login - Échec (401)")
    void login_Failure() throws Exception {
        when(authService.login(any())).thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"wrong@test.com\",\"password\":\"pwd\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Me - Récupérer profil connecté")
    void me_Success() throws Exception {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@test.com");

        // Utilisation de doReturn pour éviter les problèmes de types avec getAuthorities()
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_PLAYER")))
                .when(authentication).getAuthorities();

        mockMvc.perform(get("/auth/me").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_PLAYER"));
    }
}