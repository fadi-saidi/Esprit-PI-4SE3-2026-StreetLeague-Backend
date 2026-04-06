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
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.CoachSummary;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.PlayerSummary;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.TrainingLookupResponse;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.TrainingResponse;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.UserSummary;
import tn.esprit.pi.gestiontournoi.entity.ApprovalStatus;
import tn.esprit.pi.gestiontournoi.service.GestionTrainingService;
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
@DisplayName("GestionTrainingController")
class GestionTrainingControllerTest {

    @Mock
    private GestionTrainingService service;

    @InjectMocks
    private GestionTrainingController controller;

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
    void getAll_returnsApprovedTrainings() throws Exception {
        when(service.getApproved(any())).thenReturn(List.of(trainingResponse(1L, ApprovalStatus.APPROVED)));

        mockMvc.perform(get("/api/trainings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Morning Session"))
                .andExpect(jsonPath("$[0].coach.name").value("Coach Karim"))
                .andExpect(jsonPath("$[0].selectedPlayers[0].name").value("Player One"));
    }

    @Test
    void create_withValidBody_returnsCreatedRequest() throws Exception {
        when(service.submit(any(), any())).thenReturn(trainingResponse(2L, ApprovalStatus.PENDING));

        mockMvc.perform(post("/api/trainings")
                        .contentType("application/json")
                        .content("""
                                {
                                  "title": "Morning Session",
                                  "date": "2026-04-22",
                                  "duration": 90,
                                  "location": "Gym Hall",
                                  "selectedPlayerUserIds": [10]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.approvalStatus").value("PENDING"));
    }

    @Test
    void create_withInvalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/trainings")
                        .contentType("application/json")
                        .content("""
                                {
                                  "title": "",
                                  "date": "",
                                  "duration": 0,
                                  "location": "",
                                  "selectedPlayerUserIds": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.date").exists())
                .andExpect(jsonPath("$.errors.duration").exists())
                .andExpect(jsonPath("$.errors.location").exists())
                .andExpect(jsonPath("$.errors.selectedPlayerUserIds").exists());
    }

    @Test
    void requests_lookups_update_approve_andReject_delegateCorrectly() throws Exception {
        when(service.getRequests(any())).thenReturn(List.of(trainingResponse(3L, ApprovalStatus.PENDING)));
        TrainingLookupResponse response = new TrainingLookupResponse(
                new CoachSummary(2L, "Coach B"),
                List.of(new PlayerSummary(9L, "Player A"))
        );
        when(service.getLookups(any())).thenReturn(response);
        when(service.update(eq(3L), any(), any())).thenReturn(trainingResponse(3L, ApprovalStatus.APPROVED));
        when(service.approve(eq(3L), any(), any())).thenReturn(trainingResponse(3L, ApprovalStatus.APPROVED));
        when(service.reject(eq(3L), any(), any())).thenReturn(trainingResponse(3L, ApprovalStatus.REJECTED));

        mockMvc.perform(get("/api/trainings/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].approvalStatus").value("PENDING"));

        mockMvc.perform(get("/api/trainings/lookups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentCoach.name").value("Coach B"))
                .andExpect(jsonPath("$.availablePlayers[0].name").value("Player A"));

        mockMvc.perform(put("/api/trainings/3")
                        .contentType("application/json")
                        .content("""
                                {
                                  "title": "Morning Session",
                                  "date": "2026-04-22",
                                  "duration": 120,
                                  "location": "Gym Hall",
                                  "selectedPlayerUserIds": [10]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("APPROVED"));

        mockMvc.perform(post("/api/trainings/3/approve")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("note", "Approved"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvedBy.name").value("admin"));

        mockMvc.perform(post("/api/trainings/3/reject")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("note", "Rejected"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("REJECTED"));
    }

    @Test
    void delete_existingTraining_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/trainings/11"))
                .andExpect(status().isNoContent());

        verify(service).delete(eq(11L), any());
    }

    private TrainingResponse trainingResponse(Long id, ApprovalStatus status) {
        return new TrainingResponse(
                id,
                "Morning Session",
                new CoachSummary(4L, "Coach Karim"),
                "2026-04-22",
                90,
                "Gym Hall",
                List.of(new PlayerSummary(10L, "Player One")),
                status,
                new UserSummary(4L, "coach", "coach@test.com", "COACH"),
                new UserSummary(1L, "admin", "admin@test.com", "ADMIN"),
                null
        );
    }
}
