package tn.esprit.pi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.pi.dto.Dtos.AuthResponse;
import tn.esprit.pi.dto.Dtos.LoginRequest;
import tn.esprit.pi.dto.Dtos.RegisterRequest;
import tn.esprit.pi.domain.Role;
import tn.esprit.pi.service.IAuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = tn.esprit.pi.security.jwt.JwtAuthFilter.class))
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("deprecation")
    @MockBean
    private IAuthService authService;

    @MockBean
    private tn.esprit.pi.security.jwt.JwtService jwtService;

    @MockBean
    private tn.esprit.pi.security.CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

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

        authResponse = new AuthResponse("jwtToken", "test@example.com", "ROLE_PLAYER", 1L);
    }

    @Test
    void register_ShouldReturnSuccessMessage() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(null); // Mock user

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("User created: null")); // Since user is null
    }

    @Test
    void register_ShouldReturnBadRequestOnError() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenThrow(new IllegalArgumentException("Email already used"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: Email already used"));
    }

    @Test
    void login_ShouldReturnAuthResponse() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwtToken"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_PLAYER"));
    }

    @Test
    void login_ShouldReturnUnauthorizedOnError() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(new RuntimeException("Invalid"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid email or password"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "PLAYER")
    void me_ShouldReturnUserInfo() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.roles").isArray());
    }

    @Test
    void me_ShouldReturnUnauthorizedIfNotAuthenticated() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
