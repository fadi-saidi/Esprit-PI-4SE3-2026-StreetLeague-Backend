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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.esprit.pi.domain.Role;
import tn.esprit.pi.domain.SponsorProfile;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.repository.SponsorProfileRepository;
import tn.esprit.pi.repository.UserRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests - SponsorController")
class SponsorControllerTest {

    @Mock private SponsorProfileRepository sponsorProfileRepository;
    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;

    @InjectMocks private SponsorController sponsorController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sponsorController).build();
    }

    @Test
    @DisplayName("GET /sponsors/my-profile - Succès")
    void getMyProfile_Success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("sponsor@test.com");
        user.setRole(Role.SPONSOR);

        SponsorProfile profile = new SponsorProfile();
        profile.setCompanyName("Test Corp");

        when(authentication.getName()).thenReturn("sponsor@test.com");
        when(userRepository.findByEmail("sponsor@test.com")).thenReturn(Optional.of(user));
        when(sponsorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        mockMvc.perform(get("/sponsors/my-profile").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Test Corp"));
    }

    @Test
    @DisplayName("POST /sponsors/upload-logo - URL Invalide")
    void uploadLogo_InvalidUrl() throws Exception {
        mockMvc.perform(post("/sponsors/upload-logo")
                        .param("imageUrl", "ftp://invalid-url"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /sponsors/admin/{id} - Succès")
    void deleteSponsor_Success() throws Exception {
        when(sponsorProfileRepository.findById(1L)).thenReturn(Optional.of(new SponsorProfile()));
        mockMvc.perform(delete("/sponsors/admin/1"))
                .andExpect(status().isOk());
    }
}