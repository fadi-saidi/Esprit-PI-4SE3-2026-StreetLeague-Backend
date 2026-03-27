package tn.esprit.pi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.SponsorshipDTOs.*;
import tn.esprit.pi.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SponsorshipController.class)
@Import({SponsorshipControllerTest.JacksonConfig.class, tn.esprit.pi.security.SecurityConfig.class, tn.esprit.pi.security.PasswordConfig.class})
class SponsorshipControllerTest {

    /**
     * Registers ObjectMapper with JavaTimeModule so LocalDate fields
     * serialise correctly. @WebMvcTest does not load the full auto-configuration,
     * so we provide the bean explicitly via a static inner @Configuration.
     */
    @Configuration
    static class JacksonConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper().registerModule(new JavaTimeModule());
        }
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private SponsorshipRepository sponsorshipRepository;
    @MockitoBean private SponsorProfileRepository sponsorProfileRepository;
    @MockitoBean private UserRepository userRepository;
    @MockitoBean private TeamRepository teamRepository;
    @MockitoBean private EventRepository eventRepository;
    @MockitoBean private TournamentRepository tournamentRepository;
    @MockitoBean private VenueRepository venueRepository;
    @MockitoBean private tn.esprit.pi.security.jwt.JwtAuthFilter jwtAuthFilter;
    @MockitoBean private tn.esprit.pi.security.CustomUserDetailsService customUserDetailsService;

    // ─── Constants ────────────────────────────────────────────────────────────

    private static final long   SPONSORSHIP_ID       = 1L;
    private static final long   OTHER_SPONSORSHIP_ID = 2L;
    private static final long   MISSING_ID           = 99L;
    private static final long   USER_ID              = 100L;
    private static final long   OTHER_USER_ID        = 200L;
    private static final long   PROFILE_ID           = 10L;
    private static final long   OTHER_PROFILE_ID     = 20L;
    private static final long   TEAM_ID              = 5L;

    private static final String SPONSOR_EMAIL        = "sponsor@test.com";
    private static final String OTHER_SPONSOR_EMAIL  = "other@test.com";
    private static final String PLAYER_EMAIL         = "player@test.com";

    // ─── Builders ─────────────────────────────────────────────────────────────

    private User sponsorUser() {
        User u = new User();
        u.setId(USER_ID);
        u.setEmail(SPONSOR_EMAIL);
        u.setRole(Role.SPONSOR);
        return u;
    }

    private User playerUser() {
        User u = new User();
        u.setId(OTHER_USER_ID);
        u.setEmail(PLAYER_EMAIL);
        u.setRole(Role.PLAYER);
        return u;
    }

    private SponsorProfile profile(User owner) {
        SponsorProfile p = new SponsorProfile();
        p.setId(PROFILE_ID);
        p.setUser(owner);
        return p;
    }

    private SponsorProfile otherProfile() {
        User other = new User();
        other.setId(OTHER_USER_ID);
        other.setEmail(OTHER_SPONSOR_EMAIL);
        other.setRole(Role.SPONSOR);
        SponsorProfile p = new SponsorProfile();
        p.setId(OTHER_PROFILE_ID);
        p.setUser(other);
        return p;
    }

    private Sponsorship sponsorship(long id, SponsorProfile owner) {
        Sponsorship s = new Sponsorship();
        s.setId(id);
        s.setAmount(5000.0);
        s.setStartDate(LocalDate.now());
        s.setEndDate(LocalDate.now().plusMonths(12));
        s.setStatus(SponsorshipStatus.PENDING);
        s.setTargetType(SponsorshipTargetType.TEAM);
        s.setSponsorProfile(owner);
        return s;
    }

    /**
     * A valid request targeting a team.
     * TargetRef wraps the id; resolvedTargetType() derives TEAM from the non-null team field.
     */
    private CreateSponsorshipRequest teamRequest() {
        return new CreateSponsorshipRequest(
                5000.0,
                LocalDate.now(),
                LocalDate.now().plusMonths(12),
                "Official kit sponsor for the upcoming season",
                "Brand exposure on jerseys",
                new CreateSponsorshipRequest.TargetRef(TEAM_ID), // team
                null,   // event
                null,   // tournament
                null,   // venue
                null,   // targetType — resolved from team field
                null    // paymentProof
        );
    }

    private MockMultipartFile pdfFile() {
        return new MockMultipartFile(
                "file", "proof.pdf", "application/pdf",
                "dummy pdf content".getBytes()
        );
    }

    // ─── Public endpoints ─────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /sponsorships - returns all sponsorships")
    void getAllSponsorships_returnsOk() throws Exception {
        given(sponsorshipRepository.findAll()).willReturn(List.of(new Sponsorship()));

        mockMvc.perform(get("/sponsorships"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /sponsorships/{id} - returns the sponsorship when found")
    void getSponsorshipById_found() throws Exception {
        Sponsorship s = new Sponsorship();
        s.setId(SPONSORSHIP_ID);
        given(sponsorshipRepository.findById(SPONSORSHIP_ID)).willReturn(Optional.of(s));

        mockMvc.perform(get("/sponsorships/{id}", SPONSORSHIP_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /sponsorships/{id} - returns 404 when the sponsorship is not found")
    void getSponsorshipById_notFound() throws Exception {
        given(sponsorshipRepository.findById(MISSING_ID)).willReturn(Optional.empty());

        mockMvc.perform(get("/sponsorships/{id}", MISSING_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /sponsorships/pending - returns pending sponsorships")
    void getPendingSponsorships_returnsOk() throws Exception {
        given(sponsorshipRepository.findByStatus(SponsorshipStatus.PENDING))
                .willReturn(List.of(new Sponsorship()));

        mockMvc.perform(get("/sponsorships/pending"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /sponsorships/active - returns active sponsorships")
    void getActiveSponsorships_returnsOk() throws Exception {
        given(sponsorshipRepository.findByStatus(SponsorshipStatus.ACTIVE))
                .willReturn(List.of(new Sponsorship()));

        mockMvc.perform(get("/sponsorships/active"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /sponsorships/team/{teamId} - returns sponsorships for a team")
    void getSponsorshipsByTeam_returnsOk() throws Exception {
        given(sponsorshipRepository.findByTeamId(TEAM_ID)).willReturn(List.of(new Sponsorship()));

        mockMvc.perform(get("/sponsorships/team/{id}", TEAM_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /sponsorships/event/{eventId} - returns sponsorships for an event")
    void getSponsorshipsByEvent_returnsOk() throws Exception {
        given(sponsorshipRepository.findByEventId(TEAM_ID)).willReturn(List.of(new Sponsorship()));

        mockMvc.perform(get("/sponsorships/event/{id}", TEAM_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /sponsorships/venue/{venueId} - returns sponsorships for a venue")
    void getSponsorshipsByVenue_returnsOk() throws Exception {
        given(sponsorshipRepository.findByVenueId(TEAM_ID)).willReturn(List.of(new Sponsorship()));

        mockMvc.perform(get("/sponsorships/venue/{id}", TEAM_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /sponsorships/available-targets - returns all target types when no filter is applied")
    void getAvailableTargets_noFilter_returnsAll() throws Exception {
        Team team = new Team();
        team.setId(TEAM_ID);
        team.setName("Team A");

        given(teamRepository.findAll()).willReturn(List.of(team));
        given(eventRepository.findAll()).willReturn(List.of());
        given(tournamentRepository.findAll()).willReturn(List.of());
        given(venueRepository.findAll()).willReturn(List.of());

        mockMvc.perform(get("/sponsorships/available-targets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targets").isArray())
                .andExpect(jsonPath("$.targets[0].type").value("TEAM"));
    }

    @Test
    @DisplayName("GET /sponsorships/available-targets?type=TEAM - returns only teams")
    void getAvailableTargets_teamFilter_returnsOnlyTeams() throws Exception {
        Team team = new Team();
        team.setId(TEAM_ID);
        team.setName("Team A");
        given(teamRepository.findAll()).willReturn(List.of(team));

        mockMvc.perform(get("/sponsorships/available-targets").param("type", "TEAM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targets[0].type").value("TEAM"));
    }

    // ─── Sponsor endpoints ────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = SPONSOR_EMAIL, roles = "SPONSOR")
    @DisplayName("POST /sponsorships/submit - creates a sponsorship for a valid sponsor with a team target")
    void submitSponsorshipRequest_success() throws Exception {
        User user = sponsorUser();
        SponsorProfile p = profile(user);
        Team team = new Team();
        team.setId(TEAM_ID);

        given(userRepository.findByEmail(SPONSOR_EMAIL)).willReturn(Optional.of(user));
        given(sponsorProfileRepository.findByUserId(USER_ID)).willReturn(Optional.of(p));
        given(teamRepository.findById(TEAM_ID)).willReturn(Optional.of(team));
        given(sponsorshipRepository.save(any(Sponsorship.class))).willAnswer(inv -> {
            Sponsorship saved = inv.getArgument(0);
            saved.setId(SPONSORSHIP_ID);
            return saved;
        });

        mockMvc.perform(post("/sponsorships/submit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamRequest())))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = PLAYER_EMAIL, roles = "PLAYER")
    @DisplayName("POST /sponsorships/submit - returns 403 when the user role is not SPONSOR")
    void submitSponsorshipRequest_forbiddenForNonSponsor() throws Exception {
        given(userRepository.findByEmail(PLAYER_EMAIL)).willReturn(Optional.of(playerUser()));

        mockMvc.perform(post("/sponsorships/submit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /sponsorships/submit - returns 401 when the request is not authenticated")
    void submitSponsorshipRequest_unauthorizedWhenAnonymous() throws Exception {
        mockMvc.perform(post("/sponsorships/submit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = SPONSOR_EMAIL, roles = "SPONSOR")
    @DisplayName("GET /sponsorships/my-sponsorships - returns the list for an authenticated sponsor")
    void getMySponsorships_returnsOk() throws Exception {
        User user = sponsorUser();
        SponsorProfile p = profile(user);

        given(userRepository.findByEmail(SPONSOR_EMAIL)).willReturn(Optional.of(user));
        given(sponsorProfileRepository.findByUserId(USER_ID)).willReturn(Optional.of(p));
        given(sponsorshipRepository.findBySponsorProfile(p))
                .willReturn(List.of(sponsorship(SPONSORSHIP_ID, p)));

        mockMvc.perform(get("/sponsorships/my-sponsorships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = SPONSOR_EMAIL, roles = "SPONSOR")
    @DisplayName("PUT /sponsorships/{id}/renew - extends the end date for the sponsorship owner")
    void renewSponsorship_success() throws Exception {
        User user = sponsorUser();
        SponsorProfile p = profile(user);
        Sponsorship s = sponsorship(SPONSORSHIP_ID, p);

        given(sponsorshipRepository.findById(SPONSORSHIP_ID)).willReturn(Optional.of(s));
        given(userRepository.findByEmail(SPONSOR_EMAIL)).willReturn(Optional.of(user));
        given(sponsorProfileRepository.findByUserId(USER_ID)).willReturn(Optional.of(p));
        given(sponsorshipRepository.save(any())).willReturn(s);

        mockMvc.perform(put("/sponsorships/{id}/renew", SPONSORSHIP_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RenewSponsorshipRequest(6))))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = SPONSOR_EMAIL, roles = "SPONSOR")
    @DisplayName("PUT /sponsorships/{id}/renew - returns 404 when the sponsorship is not found")
    void renewSponsorship_notFound() throws Exception {
        given(sponsorshipRepository.findById(MISSING_ID)).willReturn(Optional.empty());

        mockMvc.perform(put("/sponsorships/{id}/renew", MISSING_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RenewSponsorshipRequest(3))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = SPONSOR_EMAIL, roles = "SPONSOR")
    @DisplayName("DELETE /sponsorships/{id}/cancel - a sponsor can cancel their own sponsorship")
    void cancelSponsorship_success() throws Exception {
        User user = sponsorUser();
        SponsorProfile p = profile(user);

        given(sponsorshipRepository.findById(SPONSORSHIP_ID))
                .willReturn(Optional.of(sponsorship(SPONSORSHIP_ID, p)));
        given(userRepository.findByEmail(SPONSOR_EMAIL)).willReturn(Optional.of(user));
        given(sponsorProfileRepository.findByUserId(USER_ID)).willReturn(Optional.of(p));

        mockMvc.perform(delete("/sponsorships/{id}/cancel", SPONSORSHIP_ID).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = SPONSOR_EMAIL, roles = "SPONSOR")
    @DisplayName("DELETE /sponsorships/{id}/cancel - returns 404 when the sponsorship is not found")
    void cancelSponsorship_notFound() throws Exception {
        given(sponsorshipRepository.findById(MISSING_ID)).willReturn(Optional.empty());

        mockMvc.perform(delete("/sponsorships/{id}/cancel", MISSING_ID).with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ─── File upload ──────────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = SPONSOR_EMAIL, roles = "SPONSOR")
    @DisplayName("POST /sponsorships/{id}/payment-proof-file - uploads a file for the sponsorship owner")
    void uploadPaymentProofFile_success() throws Exception {
        User user = sponsorUser();
        SponsorProfile p = profile(user);

        given(sponsorshipRepository.findById(SPONSORSHIP_ID))
                .willReturn(Optional.of(sponsorship(SPONSORSHIP_ID, p)));
        given(userRepository.findByEmail(SPONSOR_EMAIL)).willReturn(Optional.of(user));
        given(sponsorProfileRepository.findByUserId(USER_ID)).willReturn(Optional.of(p));
        given(sponsorshipRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(multipart("/sponsorships/{id}/payment-proof-file", SPONSORSHIP_ID)
                        .file(pdfFile())
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = SPONSOR_EMAIL, roles = "SPONSOR")
    @DisplayName("POST /sponsorships/{id}/payment-proof-file - returns 400 when the file is empty")
    void uploadPaymentProofFile_emptyFile_returnsBadRequest() throws Exception {
        MockMultipartFile empty = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]
        );

        mockMvc.perform(multipart("/sponsorships/{id}/payment-proof-file", SPONSORSHIP_ID)
                        .file(empty)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = OTHER_SPONSOR_EMAIL, roles = "SPONSOR")
    @DisplayName("POST /sponsorships/{id}/payment-proof-file - returns 403 when the profile does not own the sponsorship")
    void uploadPaymentProofFile_forbidden_wrongOwner() throws Exception {
        // Sponsorship is owned by profile id=10; requester resolves to profile id=20
        User owner = sponsorUser();
        SponsorProfile ownerProfile = profile(owner);

        User other = new User();
        other.setId(OTHER_USER_ID);
        other.setEmail(OTHER_SPONSOR_EMAIL);
        other.setRole(Role.SPONSOR);

        given(sponsorshipRepository.findById(SPONSORSHIP_ID))
                .willReturn(Optional.of(sponsorship(SPONSORSHIP_ID, ownerProfile)));
        given(userRepository.findByEmail(OTHER_SPONSOR_EMAIL)).willReturn(Optional.of(other));
        given(sponsorProfileRepository.findByUserId(OTHER_USER_ID))
                .willReturn(Optional.of(otherProfile()));

        mockMvc.perform(multipart("/sponsorships/{id}/payment-proof-file", SPONSORSHIP_ID)
                        .file(pdfFile())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ─── Admin endpoints ──────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /sponsorships/admin/stats - returns stats with the correct values")
    void getAdminStats_returnsCorrectStats() throws Exception {
        given(sponsorshipRepository.getStatsForPeriod(any(), any()))
                .willReturn(List.<Object[]>of(new Object[]{10L, 45000.0}));
        given(sponsorshipRepository.findByStatus(SponsorshipStatus.PENDING))
                .willReturn(List.of());

        mockMvc.perform(get("/sponsorships/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalActive").value(10))
                .andExpect(jsonPath("$.totalAmountThisMonth").value(45000.0))
                .andExpect(jsonPath("$.totalPending").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /sponsorships/admin/stats - returns zeros when there are no active sponsorships")
    void getAdminStats_emptyStats_returnsZeros() throws Exception {
        given(sponsorshipRepository.getStatsForPeriod(any(), any())).willReturn(List.of());
        given(sponsorshipRepository.findByStatus(SponsorshipStatus.PENDING)).willReturn(List.of());

        mockMvc.perform(get("/sponsorships/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalActive").value(0))
                .andExpect(jsonPath("$.totalAmountThisMonth").value(0.0))
                .andExpect(jsonPath("$.totalPending").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /sponsorships/admin/{id}/approve - sets the status to ACTIVE")
    void approveSponsorship_setsStatusActive() throws Exception {
        Sponsorship s = new Sponsorship();
        s.setId(SPONSORSHIP_ID);
        s.setStatus(SponsorshipStatus.PENDING);

        given(sponsorshipRepository.findById(SPONSORSHIP_ID)).willReturn(Optional.of(s));
        given(sponsorshipRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/sponsorships/admin/{id}/approve", SPONSORSHIP_ID).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /sponsorships/admin/{id}/approve - returns 404 when the sponsorship is not found")
    void approveSponsorship_notFound() throws Exception {
        given(sponsorshipRepository.findById(MISSING_ID)).willReturn(Optional.empty());

        mockMvc.perform(put("/sponsorships/admin/{id}/approve", MISSING_ID).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /sponsorships/admin/{id}/reject - sets the status to REJECTED")
    void rejectSponsorship_setsStatusRejected() throws Exception {
        Sponsorship s = new Sponsorship();
        s.setId(OTHER_SPONSORSHIP_ID);
        s.setStatus(SponsorshipStatus.PENDING);

        given(sponsorshipRepository.findById(OTHER_SPONSORSHIP_ID)).willReturn(Optional.of(s));
        given(sponsorshipRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/sponsorships/admin/{id}/reject", OTHER_SPONSORSHIP_ID).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /sponsorships/admin/{id}/reject - returns 404 when the sponsorship is not found")
    void rejectSponsorship_notFound() throws Exception {
        given(sponsorshipRepository.findById(MISSING_ID)).willReturn(Optional.empty());

        mockMvc.perform(put("/sponsorships/admin/{id}/reject", MISSING_ID).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /sponsorships/admin/{id} - updates the allowed fields")
    void updateSponsorship_updatesFields() throws Exception {
        Sponsorship s = new Sponsorship();
        s.setId(SPONSORSHIP_ID);
        s.setAmount(1000.0);

        given(sponsorshipRepository.findById(SPONSORSHIP_ID)).willReturn(Optional.of(s));
        given(sponsorshipRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        UpdateSponsorshipRequest request = new UpdateSponsorshipRequest(
                9999.0,
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                "Updated description that is long enough to pass validation",
                "Updated benefits here"
        );

        mockMvc.perform(put("/sponsorships/admin/{id}", SPONSORSHIP_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /sponsorships/admin/{id} - returns 404 when the sponsorship is not found")
    void updateSponsorship_notFound() throws Exception {
        given(sponsorshipRepository.findById(MISSING_ID)).willReturn(Optional.empty());

        mockMvc.perform(put("/sponsorships/admin/{id}", MISSING_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateSponsorshipRequest(100.0, null, null, null, null))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /sponsorships/admin/{id} - deletes a sponsorship successfully")
    void deleteSponsorshipAdmin_success() throws Exception {
        given(sponsorshipRepository.findById(SPONSORSHIP_ID))
                .willReturn(Optional.of(new Sponsorship()));

        mockMvc.perform(delete("/sponsorships/admin/{id}", SPONSORSHIP_ID).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /sponsorships/admin/{id} - returns 404 when the sponsorship is not found")
    void deleteSponsorshipAdmin_notFound() throws Exception {
        given(sponsorshipRepository.findById(MISSING_ID)).willReturn(Optional.empty());

        mockMvc.perform(delete("/sponsorships/admin/{id}", MISSING_ID).with(csrf()))
                .andExpect(status().isNotFound());
    }
}