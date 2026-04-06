package tn.esprit.pi.gestiontournoi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.MatchLookupResponse;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.MatchResponse;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.RefereeSummary;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.TeamSummary;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.UserSummary;
import tn.esprit.pi.gestiontournoi.entity.ApprovalStatus;
import tn.esprit.pi.gestiontournoi.service.GestionMatchService;
import tn.esprit.pi.security.GlobalExceptionHandler;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("GestionMatchController")
class GestionMatchControllerTest {

    @Mock
    private GestionMatchService service;

    @InjectMocks
    private GestionMatchController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void getAll_returnsApprovedMatches() throws Exception {
        when(service.getApproved(any())).thenReturn(List.of(matchResponse(1L, ApprovalStatus.APPROVED)));

        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].homeTeam.name").value("Team A"))
                .andExpect(jsonPath("$[0].awayTeam.name").value("Team B"))
                .andExpect(jsonPath("$[0].referee.name").value("Ref One"));
    }

    @Test
    void create_withValidBody_returnsCreatedRequest() throws Exception {
        when(service.submit(any(), any())).thenReturn(matchResponse(2L, ApprovalStatus.PENDING));

        mockMvc.perform(post("/api/matches")
                        .contentType("application/json")
                        .content("""
                                {
                                  "homeTeamId": 1,
                                  "awayTeamId": 2,
                                  "refereeUserId": 9,
                                  "date": "2026-04-10",
                                  "score": "1-0",
                                  "location": "Pitch 1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("PENDING"));
    }

    @Test
    void create_withInvalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/matches")
                        .contentType("application/json")
                        .content("""
                                {
                                  "date": "",
                                  "score": "0-0",
                                  "location": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.homeTeamId").exists())
                .andExpect(jsonPath("$.errors.awayTeamId").exists())
                .andExpect(jsonPath("$.errors.refereeUserId").exists())
                .andExpect(jsonPath("$.errors.date").exists())
                .andExpect(jsonPath("$.errors.location").exists());
    }

    @Test
    void getRequests_lookups_update_approve_andReject_delegateCorrectly() throws Exception {
        when(service.getRequests(any())).thenReturn(List.of(matchResponse(3L, ApprovalStatus.PENDING)));
        when(service.getLookups(any())).thenReturn(new MatchLookupResponse(
                List.of(new TeamSummary(1L, "Street Wolves")),
                List.of(new RefereeSummary(8L, "Ref Karim"))
        ));
        when(service.update(eq(3L), any(), any())).thenReturn(matchResponse(3L, ApprovalStatus.APPROVED));
        when(service.approve(eq(3L), any(), any())).thenReturn(matchResponse(3L, ApprovalStatus.APPROVED));
        when(service.reject(eq(3L), any(), any())).thenReturn(matchResponse(3L, ApprovalStatus.REJECTED));

        mockMvc.perform(get("/api/matches/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].approvalStatus").value("PENDING"));

        mockMvc.perform(get("/api/matches/lookups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teams[0].name").value("Street Wolves"))
                .andExpect(jsonPath("$.referees[0].name").value("Ref Karim"));

        mockMvc.perform(put("/api/matches/3")
                        .contentType("application/json")
                        .content("""
                                {
                                  "homeTeamId": 1,
                                  "awayTeamId": 2,
                                  "refereeUserId": 9,
                                  "date": "2026-04-20",
                                  "score": "3-2",
                                  "location": "Field 2"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("APPROVED"));

        mockMvc.perform(post("/api/matches/3/approve")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("note", "Approved"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("APPROVED"));

        mockMvc.perform(post("/api/matches/3/reject")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("note", "Rejected"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("REJECTED"));
    }

    @Test
    void delete_existingMatch_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/matches/4"))
                .andExpect(status().isNoContent());

        verify(service).delete(eq(4L), any());
    }

    private MatchResponse matchResponse(Long id, ApprovalStatus status) {
        return new MatchResponse(
                id,
                new TeamSummary(10L, "Team A"),
                new TeamSummary(11L, "Team B"),
                new RefereeSummary(6L, "Ref One"),
                "2026-04-15",
                "1-0",
                "Pitch 1",
                status,
                new UserSummary(4L, "user1", "user1@test.com", "PLAYER"),
                new UserSummary(1L, "admin", "admin@test.com", "ADMIN"),
                null
        );
    }
}
