package tn.esprit.pi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.SponsorshipDTOs;
import tn.esprit.pi.repository.*;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SponsorshipControllerTest {

    @Mock private SponsorshipRepository sponsorshipRepository;
    @Mock private UserRepository userRepository;
    @Mock private SponsorProfileRepository sponsorProfileRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private Authentication authentication;
    @Mock private VenueRepository venueRepository;

    @InjectMocks private SponsorshipController sponsorshipController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sponsorshipController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Pour gérer LocalDate
    }

    @Test
    @DisplayName("POST /submit - Succès pour une Team")
    void submitSponsorship_Team_Success() throws Exception {
        // Setup User & Profile
        User user = new User();
        user.setId(1L);
        user.setRole(Role.SPONSOR);
        user.setEmail("sponsor@test.com");

        SponsorProfile profile = new SponsorProfile();
        profile.setId(10L);

        Team team = new Team();
        team.setId(100L);
        team.setName("Esprit Team");

        SponsorshipDTOs.CreateSponsorshipRequest request = new SponsorshipDTOs.CreateSponsorshipRequest(
                5000.0, LocalDate.now(), LocalDate.now().plusMonths(6), "Desc", "Benefits",
                new SponsorshipDTOs.CreateSponsorshipRequest.TargetRef(100L), null, null, null, null, "proof.png"
        );

        when(authentication.getName()).thenReturn("sponsor@test.com");
        when(userRepository.findByEmail("sponsor@test.com")).thenReturn(Optional.of(user));
        when(sponsorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(teamRepository.findById(100L)).thenReturn(Optional.of(team));
        when(sponsorshipRepository.save(any(Sponsorship.class))).thenAnswer(i -> i.getArguments()[0]);

        mockMvc.perform(post("/sponsorships/submit")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(5000.0))
                .andExpect(jsonPath("$.targetName").value("Esprit Team"));
    }

    @Test
    @DisplayName("GET /available-targets - Filtrage par type VENUE")
    void getAvailableTargets_VenueOnly() throws Exception {
        mockMvc.perform(get("/sponsorships/available-targets").param("type", "VENUE"))
                .andExpect(status().isOk());

        verify(venueRepository, atLeastOnce()).findAll();
        verifyNoInteractions(teamRepository); // Ne doit pas appeler team si type=VENUE
    }

    @Test
    @DisplayName("PUT /renew - Succès")
    void renewSponsorship_Success() throws Exception {
        Sponsorship s = new Sponsorship();
        s.setId(1L);
        s.setEndDate(LocalDate.now());
        SponsorProfile p = new SponsorProfile();
        s.setSponsorProfile(p);

        User user = new User();
        user.setId(1L);
        user.setRole(Role.SPONSOR);

        SponsorshipDTOs.RenewSponsorshipRequest renewReq = new SponsorshipDTOs.RenewSponsorshipRequest(6);

        when(sponsorshipRepository.findById(1L)).thenReturn(Optional.of(s));
        when(authentication.getName()).thenReturn("user");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(sponsorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(p));
        when(sponsorshipRepository.save(any())).thenReturn(s);

        mockMvc.perform(put("/sponsorships/1/renew")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(renewReq)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /submit - Échec si non Sponsor")
    void submitSponsorship_Forbidden() throws Exception {
        User user = new User();
        user.setRole(Role.PLAYER); // Pas un sponsor

        when(authentication.getName()).thenReturn("player@test.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        SponsorshipDTOs.CreateSponsorshipRequest request = new SponsorshipDTOs.CreateSponsorshipRequest(
                100.0, LocalDate.now(), LocalDate.now(), "D", "B", null, null, null, null, null, "P"
        );

        mockMvc.perform(post("/sponsorships/submit")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /admin/{id} - Succès")
    void deleteAdmin_Success() throws Exception {
        when(sponsorshipRepository.findById(1L)).thenReturn(Optional.of(new Sponsorship()));

        mockMvc.perform(delete("/sponsorships/admin/1"))
                .andExpect(status().isOk());

        verify(sponsorshipRepository).delete(any());
    }
}