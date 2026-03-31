package tn.esprit.pi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.SponsorshipDTOs.*;
import tn.esprit.pi.repository.*;
import tn.esprit.pi.security.jwt.JwtService;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SponsorshipController.class)
class SponsorshipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SponsorshipRepository sponsorshipRepository;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private SponsorProfileRepository sponsorProfileRepository;
    @MockitoBean
    private TeamRepository teamRepository;
    @MockitoBean
    private EventRepository eventRepository;
    @MockitoBean
    private TournamentRepository tournamentRepository;
    @MockitoBean
    private VenueRepository venueRepository;
    @MockitoBean
    private JwtService jwtService;

    private ObjectMapper objectMapper;
    private User user;
    private SponsorProfile sponsorProfile;
    private Sponsorship sponsorship;
    private Team team;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        user = new User();
        user.setId(1L);
        user.setEmail("sponsor@test.com");
        user.setRole(Role.SPONSOR);

        sponsorProfile = new SponsorProfile();
        sponsorProfile.setId(1L);
        sponsorProfile.setUser(user);
        sponsorProfile.setCompanyName("Test Company");

        team = new Team();
        team.setId(1L);
        team.setName("Test Team");
        team.setSportType(SportType.FOOTBALL);

        sponsorship = new Sponsorship();
        sponsorship.setId(1L);
        sponsorship.setAmount(1000.0);
        sponsorship.setStartDate(LocalDate.now());
        sponsorship.setEndDate(LocalDate.now().plusMonths(6));
        sponsorship.setStatus(SponsorshipStatus.PENDING);
        sponsorship.setTargetType(SponsorshipTargetType.TEAM);
        sponsorship.setSponsorProfile(sponsorProfile);
        sponsorship.setTeam(team);
    }

    @Test
    void getSponsorshipById_ShouldReturnSponsorship() throws Exception {
        when(sponsorshipRepository.findById(1L)).thenReturn(Optional.of(sponsorship));

        mockMvc.perform(get("/sponsorships/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.amount").value(1000.0))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getSponsorshipById_ShouldReturnNotFound() throws Exception {
        when(sponsorshipRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/sponsorships/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSponsorshipsByTeam_ShouldReturnList() throws Exception {
        List<Sponsorship> sponsorships = Arrays.asList(sponsorship);
        when(sponsorshipRepository.findByTeamId(1L)).thenReturn(sponsorships);

        mockMvc.perform(get("/sponsorships/team/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @WithMockUser(username = "sponsor@test.com")
    void submitSponsorshipRequest_ShouldCreateSponsorship() throws Exception {
        CreateSponsorshipRequest request = new CreateSponsorshipRequest(
                1000.0,
                LocalDate.now(),
                LocalDate.now().plusMonths(6),
                "Test sponsorship",
                "Brand visibility",
                new CreateSponsorshipRequest.TargetRef(1L), // team
                null, // event
                null, // tournament
                null, // venue
                null, // targetType
                null  // paymentProof
        );

        when(userRepository.findByEmail("sponsor@test.com")).thenReturn(Optional.of(user));
        when(sponsorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(sponsorProfile));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(sponsorshipRepository.save(any(Sponsorship.class))).thenReturn(sponsorship);

        mockMvc.perform(post("/sponsorships/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(username = "player@test.com")
    void submitSponsorshipRequest_ShouldReturnForbiddenForNonSponsor() throws Exception {
        CreateSponsorshipRequest request = new CreateSponsorshipRequest(
                1000.0,
                LocalDate.now(),
                LocalDate.now().plusMonths(6),
                "Test sponsorship",
                "Brand visibility",
                new CreateSponsorshipRequest.TargetRef(1L),
                null, null, null, null, null
        );

        User playerUser = new User();
        playerUser.setRole(Role.PLAYER);

        when(userRepository.findByEmail("player@test.com")).thenReturn(Optional.of(playerUser));

        mockMvc.perform(post("/sponsorships/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAvailableTargets_ShouldReturnAllTargets() throws Exception {
        when(teamRepository.findAll()).thenReturn(Arrays.asList(team));
        when(eventRepository.findAll()).thenReturn(Arrays.asList());
        when(tournamentRepository.findAll()).thenReturn(Arrays.asList());
        when(venueRepository.findAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/sponsorships/available-targets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targets").isArray());
    }

    @Test
    @WithMockUser(username = "sponsor@test.com")
    void renewSponsorship_ShouldExtendEndDate() throws Exception {
        when(userRepository.findByEmail("sponsor@test.com")).thenReturn(Optional.of(user));
        when(sponsorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(sponsorProfile));
        when(sponsorshipRepository.findById(1L)).thenReturn(Optional.of(sponsorship));
        when(sponsorshipRepository.save(any(Sponsorship.class))).thenReturn(sponsorship);

        mockMvc.perform(put("/sponsorships/1/renew")
                        .param("months", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(username = "sponsor@test.com")
    void uploadPaymentProof_ShouldUpdateSponsorship() throws Exception {
        when(userRepository.findByEmail("sponsor@test.com")).thenReturn(Optional.of(user));
        when(sponsorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(sponsorProfile));
        when(sponsorshipRepository.findById(1L)).thenReturn(Optional.of(sponsorship));
        when(sponsorshipRepository.save(any(Sponsorship.class))).thenReturn(sponsorship);

        mockMvc.perform(post("/sponsorships/1/payment-proof")
                        .param("proofUrl", "http://example.com/proof.pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Payment proof uploaded successfully"));
    }

    @Test
    void getAllSponsorships_ShouldReturnAllSponsorships() throws Exception {
        List<Sponsorship> sponsorships = List.of(sponsorship);
        when(sponsorshipRepository.findAll()).thenReturn(sponsorships);

        mockMvc.perform(get("/sponsorships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getSponsorshipsByEvent_ShouldReturnList() throws Exception {
        List<Sponsorship> sponsorships = List.of(sponsorship);
        when(sponsorshipRepository.findByEventId(1L)).thenReturn(sponsorships);

        mockMvc.perform(get("/sponsorships/event/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getSponsorshipsByTournament_ShouldReturnList() throws Exception {
        List<Sponsorship> sponsorships = List.of(sponsorship);
        when(sponsorshipRepository.findByTournamentId(1L)).thenReturn(sponsorships);

        mockMvc.perform(get("/sponsorships/tournament/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getSponsorshipsByVenue_ShouldReturnList() throws Exception {
        List<Sponsorship> sponsorships = List.of(sponsorship);
        when(sponsorshipRepository.findByVenueId(1L)).thenReturn(sponsorships);

        mockMvc.perform(get("/sponsorships/venue/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @WithMockUser(username = "sponsor@test.com")
    void getMySponsorships_ShouldReturnSponsorSponsorships() throws Exception {
        List<Sponsorship> sponsorships = List.of(sponsorship);
        when(userRepository.findByEmail("sponsor@test.com")).thenReturn(Optional.of(user));
        when(sponsorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(sponsorProfile));
        when(sponsorshipRepository.findBySponsorProfile(sponsorProfile)).thenReturn(sponsorships);

        mockMvc.perform(get("/sponsorships/my-sponsorships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    void approveSponsorshipRequest_ShouldApproveSponsorship() throws Exception {
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setRole(Role.ADMIN);

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(sponsorshipRepository.findById(1L)).thenReturn(Optional.of(sponsorship));
        when(sponsorshipRepository.save(any(Sponsorship.class))).thenReturn(sponsorship);

        mockMvc.perform(put("/sponsorships/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    void rejectSponsorshipRequest_ShouldRejectSponsorship() throws Exception {
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setRole(Role.ADMIN);

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(sponsorshipRepository.findById(1L)).thenReturn(Optional.of(sponsorship));
        when(sponsorshipRepository.save(any(Sponsorship.class))).thenReturn(sponsorship);

        mockMvc.perform(put("/sponsorships/1/reject")
                        .param("reason", "Insufficient budget"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(username = "sponsor@test.com")
    void cancelSponsorship_ShouldCancelSponsorship() throws Exception {
        when(userRepository.findByEmail("sponsor@test.com")).thenReturn(Optional.of(user));
        when(sponsorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(sponsorProfile));
        when(sponsorshipRepository.findById(1L)).thenReturn(Optional.of(sponsorship));
        when(sponsorshipRepository.save(any(Sponsorship.class))).thenReturn(sponsorship);

        mockMvc.perform(put("/sponsorships/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(username = "player@test.com")
    void approveSponsorshipRequest_ShouldReturnForbiddenForNonAdmin() throws Exception {
        User playerUser = new User();
        playerUser.setRole(Role.PLAYER);

        when(userRepository.findByEmail("player@test.com")).thenReturn(Optional.of(playerUser));

        mockMvc.perform(put("/sponsorships/1/approve"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "sponsor@test.com")
    void renewSponsorship_ShouldReturnBadRequestForInvalidMonths() throws Exception {
        when(userRepository.findByEmail("sponsor@test.com")).thenReturn(Optional.of(user));
        when(sponsorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(sponsorProfile));
        when(sponsorshipRepository.findById(1L)).thenReturn(Optional.of(sponsorship));

        mockMvc.perform(put("/sponsorships/1/renew")
                        .param("months", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSponsorshipById_ShouldReturnBadRequestForInvalidId() throws Exception {
        mockMvc.perform(get("/sponsorships/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "sponsor@test.com")
    void submitSponsorshipRequest_ShouldReturnBadRequestForInvalidAmount() throws Exception {
        CreateSponsorshipRequest request = new CreateSponsorshipRequest(
                -1000.0, // Invalid negative amount
                LocalDate.now(),
                LocalDate.now().plusMonths(6),
                "Test sponsorship",
                "Brand visibility",
                new CreateSponsorshipRequest.TargetRef(1L),
                null, null, null, null, null
        );

        when(userRepository.findByEmail("sponsor@test.com")).thenReturn(Optional.of(user));
        when(sponsorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(sponsorProfile));

        mockMvc.perform(post("/sponsorships/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "sponsor@test.com")
    void submitSponsorshipRequest_ShouldReturnBadRequestForInvalidDates() throws Exception {
        CreateSponsorshipRequest request = new CreateSponsorshipRequest(
                1000.0,
                LocalDate.now().plusMonths(6), // End date before start date
                LocalDate.now(),
                "Test sponsorship",
                "Brand visibility",
                new CreateSponsorshipRequest.TargetRef(1L),
                null, null, null, null, null
        );

        when(userRepository.findByEmail("sponsor@test.com")).thenReturn(Optional.of(user));
        when(sponsorProfileRepository.findByUserId(1L)).thenReturn(Optional.of(sponsorProfile));

        mockMvc.perform(post("/sponsorships/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}