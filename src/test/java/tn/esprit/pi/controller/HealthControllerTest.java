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
import tn.esprit.pi.domain.InjurySeverity;
import tn.esprit.pi.dto.Dtos.InjuryDTO;
import tn.esprit.pi.dto.Dtos.MedicalRecordDTO;
import tn.esprit.pi.dto.Dtos.RecommendationRequest;
import tn.esprit.pi.service.health.IHealthService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = HealthController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = tn.esprit.pi.security.jwt.JwtAuthFilter.class))
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("deprecation")
    @MockBean
    private IHealthService healthService;

    @MockBean
    private tn.esprit.pi.security.jwt.JwtService jwtService;

    @MockBean
    private tn.esprit.pi.security.CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private MedicalRecordDTO medicalRecordDTO;
    private InjuryDTO injuryDTO;

    @BeforeEach
    void setUp() {
        medicalRecordDTO = new MedicalRecordDTO(1L, 70.0, 175.0, "A+", null, null, LocalDate.now(), 1L, 1L);
        injuryDTO = new InjuryDTO(1L, "Sprained ankle", "Rest", LocalDate.now(), InjurySeverity.MODERATE, 1L);
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void createRecord_ShouldReturnCreatedRecord() throws Exception {
        when(healthService.createRecord(any(MedicalRecordDTO.class))).thenReturn(medicalRecordDTO);

        mockMvc.perform(post("/medical/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicalRecordDTO))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.weight").value(70.0));
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void updateRecord_ShouldReturnUpdatedRecord() throws Exception {
        when(healthService.updateRecord(eq(1L), any(MedicalRecordDTO.class))).thenReturn(medicalRecordDTO);

        mockMvc.perform(put("/medical/records/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicalRecordDTO))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "HEALTH_PROFESSIONAL")
    void getAllRecords_ShouldReturnList() throws Exception {
        List<MedicalRecordDTO> records = Arrays.asList(medicalRecordDTO);
        when(healthService.getAllRecords()).thenReturn(records);

        mockMvc.perform(get("/medical/records/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    void getRecord_ShouldReturnRecord() throws Exception {
        when(healthService.getRecordById(1L)).thenReturn(medicalRecordDTO);

        mockMvc.perform(get("/medical/records/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteRecord_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/medical/records/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void declareInjury_ShouldReturnCreatedInjury() throws Exception {
        when(healthService.declareInjury(any(InjuryDTO.class))).thenReturn(injuryDTO);

        mockMvc.perform(post("/medical/injuries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(injuryDTO))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.report").value("Sprained ankle"));
    }

    @Test
    @WithMockUser(roles = "HEALTH_PROFESSIONAL")
    void addRecommendation_ShouldReturnUpdatedInjury() throws Exception {
        RecommendationRequest request = new RecommendationRequest("Rest for 2 weeks");
        when(healthService.addRecommendation(1L, "Rest for 2 weeks")).thenReturn(injuryDTO);

        mockMvc.perform(put("/medical/injuries/1/recommendation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendation").value("Rest"));
    }

    @Test
    @WithMockUser
    void getInjury_ShouldReturnInjury() throws Exception {
        when(healthService.getInjuryById(1L)).thenReturn(injuryDTO);

        mockMvc.perform(get("/medical/injuries/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser
    void getInjuriesByRecord_ShouldReturnList() throws Exception {
        List<InjuryDTO> injuries = Arrays.asList(injuryDTO);
        when(healthService.getInjuriesByRecord(1L)).thenReturn(injuries);

        mockMvc.perform(get("/medical/injuries/record/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    void filterBySeverity_ShouldReturnFilteredList() throws Exception {
        List<InjuryDTO> injuries = Arrays.asList(injuryDTO);
        when(healthService.filterBySeverity(1L, InjurySeverity.MODERATE)).thenReturn(injuries);

        mockMvc.perform(get("/medical/injuries/record/1/filter")
                        .param("severity", "MODERATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "HEALTH_PROFESSIONAL")
    void getAllInjuries_ShouldReturnList() throws Exception {
        List<InjuryDTO> injuries = Arrays.asList(injuryDTO);
        when(healthService.getAllInjuries()).thenReturn(injuries);

        mockMvc.perform(get("/medical/injuries/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
