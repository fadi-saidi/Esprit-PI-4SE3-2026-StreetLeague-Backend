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
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.TournamentResponse;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.UserSummary;
import tn.esprit.pi.gestiontournoi.entity.ApprovalStatus;
import tn.esprit.pi.gestiontournoi.service.GestionTournamentService;
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
@DisplayName("GestionTournamentController")
class GestionTournamentControllerTest {

    @Mock
    private GestionTournamentService service;

    @InjectMocks
    private GestionTournamentController controller;

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
    void getAll_returnsApprovedTournaments() throws Exception {
        when(service.getApproved(any())).thenReturn(List.of(tournamentResponse(1L, ApprovalStatus.APPROVED, false)));

        mockMvc.perform(get("/api/tournaments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Summer Cup"))
                .andExpect(jsonPath("$[0].maxTeams").value(8))
                .andExpect(jsonPath("$[0].participantCount").value(6));
    }

    @Test
    void create_withValidBody_returnsCreatedRequest() throws Exception {
        when(service.submit(any(), any())).thenReturn(tournamentResponse(2L, ApprovalStatus.PENDING, false));

        mockMvc.perform(post("/api/tournaments")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Autumn Cup",
                                  "startDate": "2026-06-01",
                                  "endDate": "2026-06-04",
                                  "location": "Arena",
                                  "maxTeams": 8
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.approvalStatus").value("PENDING"));
    }

    @Test
    void create_withInvalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/tournaments")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "",
                                  "startDate": "",
                                  "endDate": "",
                                  "location": "",
                                  "maxTeams": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.startDate").exists())
                .andExpect(jsonPath("$.errors.endDate").exists())
                .andExpect(jsonPath("$.errors.location").exists())
                .andExpect(jsonPath("$.errors.maxTeams").exists());
    }

    @Test
    void requests_update_approve_reject_andParticipation_routes_delegateCorrectly() throws Exception {
        when(service.getRequests(any())).thenReturn(List.of(tournamentResponse(3L, ApprovalStatus.PENDING, false)));
        when(service.update(eq(3L), any(), any())).thenReturn(tournamentResponse(3L, ApprovalStatus.APPROVED, false));
        when(service.approve(eq(3L), any(), any())).thenReturn(tournamentResponse(3L, ApprovalStatus.APPROVED, false));
        when(service.reject(eq(3L), any(), any())).thenReturn(tournamentResponse(3L, ApprovalStatus.REJECTED, false));
        when(service.participate(eq(6L), any())).thenReturn(tournamentResponse(6L, ApprovalStatus.APPROVED, true));
        when(service.cancelParticipation(eq(6L), any())).thenReturn(tournamentResponse(6L, ApprovalStatus.APPROVED, false));

        mockMvc.perform(get("/api/tournaments/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].approvalStatus").value("PENDING"));

        mockMvc.perform(put("/api/tournaments/3")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Updated Cup",
                                  "startDate": "2026-05-10",
                                  "endDate": "2026-05-12",
                                  "location": "New Arena",
                                  "maxTeams": 16
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("APPROVED"));

        mockMvc.perform(post("/api/tournaments/3/approve")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("note", "Approved"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvedBy.name").value("admin"));

        mockMvc.perform(post("/api/tournaments/3/reject")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("note", "Rejected"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("REJECTED"));

        mockMvc.perform(post("/api/tournaments/6/participate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantCount").value(7))
                .andExpect(jsonPath("$.participating").value(true));

        mockMvc.perform(delete("/api/tournaments/6/participate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participating").value(false));
    }

    @Test
    void delete_existingTournament_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/tournaments/8"))
                .andExpect(status().isNoContent());

        verify(service).delete(eq(8L), any());
    }

    private TournamentResponse tournamentResponse(Long id, ApprovalStatus status, boolean participating) {
        return new TournamentResponse(
                id,
                "Summer Cup",
                "2026-06-01",
                "2026-06-10",
                "Arena",
                8,
                status,
                new UserSummary(4L, "user1", "user1@test.com", "PLAYER"),
                new UserSummary(1L, "admin", "admin@test.com", "ADMIN"),
                null,
                6 + (participating ? 1 : 0),
                participating
        );
    }
}
