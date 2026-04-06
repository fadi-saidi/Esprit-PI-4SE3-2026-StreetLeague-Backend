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
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.EventResponse;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.UserSummary;
import tn.esprit.pi.gestiontournoi.entity.ApprovalStatus;
import tn.esprit.pi.gestiontournoi.service.GestionEventService;
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
@DisplayName("GestionEventController")
class GestionEventControllerTest {

    @Mock
    private GestionEventService service;

    @InjectMocks
    private GestionEventController controller;

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
    void getAll_returnsApprovedEvents() throws Exception {
        when(service.getApproved(any())).thenReturn(List.of(eventResponse(1L, ApprovalStatus.APPROVED, false)));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].approvalStatus").value("APPROVED"))
                .andExpect(jsonPath("$[0].participantCount").value(5));
    }

    @Test
    void create_withValidBody_returnsCreatedRequest() throws Exception {
        when(service.submit(any(), any())).thenReturn(eventResponse(2L, ApprovalStatus.PENDING, false));

        mockMvc.perform(post("/api/events")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Opening Day",
                                  "description": "desc",
                                  "date": "2026-04-10",
                                  "location": "Arena"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.approvalStatus").value("PENDING"));
    }

    @Test
    void create_withInvalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "",
                                  "description": "desc",
                                  "date": "",
                                  "location": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.date").exists())
                .andExpect(jsonPath("$.errors.location").exists());
    }

    @Test
    void getRequests_update_approve_reject_andParticipation_routes_delegateCorrectly() throws Exception {
        when(service.getRequests(any())).thenReturn(List.of(eventResponse(3L, ApprovalStatus.PENDING, false)));
        when(service.update(eq(3L), any(), any())).thenReturn(eventResponse(3L, ApprovalStatus.APPROVED, false));
        when(service.approve(eq(3L), any(), any())).thenReturn(eventResponse(3L, ApprovalStatus.APPROVED, false));
        when(service.reject(eq(3L), any(), any())).thenReturn(eventResponse(3L, ApprovalStatus.REJECTED, false));
        when(service.participate(eq(3L), any())).thenReturn(eventResponse(3L, ApprovalStatus.APPROVED, true));
        when(service.cancelParticipation(eq(3L), any())).thenReturn(eventResponse(3L, ApprovalStatus.APPROVED, false));

        mockMvc.perform(get("/api/events/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].approvalStatus").value("PENDING"));

        mockMvc.perform(put("/api/events/3")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Updated Event",
                                  "description": "desc",
                                  "date": "2026-04-11",
                                  "location": "Arena"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("APPROVED"));

        mockMvc.perform(post("/api/events/3/approve")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("note", "Looks good"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvedBy.name").value("admin"));

        mockMvc.perform(post("/api/events/3/reject")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("note", "Rejected"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("REJECTED"));

        mockMvc.perform(post("/api/events/3/participate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participating").value(true));

        mockMvc.perform(delete("/api/events/3/participate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participating").value(false));
    }

    @Test
    void delete_existingEvent_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/events/9"))
                .andExpect(status().isNoContent());

        verify(service).delete(eq(9L), any());
    }

    private EventResponse eventResponse(Long id, ApprovalStatus status, boolean participating) {
        return new EventResponse(
                id,
                "Opening Day",
                "Updated",
                "2026-04-12",
                "New Place",
                status,
                new UserSummary(5L, "requester", "requester@test.com", "PLAYER"),
                new UserSummary(1L, "admin", "admin@test.com", "ADMIN"),
                "Looks good",
                5,
                participating
        );
    }
}
