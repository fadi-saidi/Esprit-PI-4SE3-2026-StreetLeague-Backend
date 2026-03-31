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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.esprit.pi.domain.InjurySeverity;
import tn.esprit.pi.dto.Dtos;
import tn.esprit.pi.service.health.IHealthService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - HealthController (Validé)")
class HealthControllerTest {

    @Mock private IHealthService healthService;
    @InjectMocks private HealthController healthController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Dtos.MedicalRecordDTO medicalRecordDTO;
    private Dtos.InjuryDTO injuryDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(healthController).build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        medicalRecordDTO = new Dtos.MedicalRecordDTO(100L, 75.5, 180.0, "O+", "Asthme", "Pénicilline", LocalDate.now(), 1L, 10L);
        injuryDTO = new Dtos.InjuryDTO(200L, "Douleur au genou", "Repos recommandé", LocalDate.now(), InjurySeverity.MODERATE, 100L);
    }

    @Test
    @DisplayName("POST /medical/records - Succès")
    void createRecord_Success() throws Exception {
        when(healthService.createRecord(any())).thenReturn(medicalRecordDTO);

        mockMvc.perform(post("/medical/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicalRecordDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    @DisplayName("POST /medical/records - Échec Validation (Poids trop bas)")
    void createRecord_ValidationError() throws Exception {
        // Poids de 5kg (le minimum est 20kg dans notre DTO corrigé)
        Dtos.MedicalRecordDTO invalidDto = new Dtos.MedicalRecordDTO(null, 5.0, 180.0, "O+", null, null, LocalDate.now(), 1L, null);

        mockMvc.perform(post("/medical/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest()); // Doit retourner 400 à cause de @Valid
    }

    @Test
    @DisplayName("PUT /medical/injuries/{id}/recommendation - Succès")
    void addRecommendation_Success() throws Exception {
        Dtos.RecommendationRequest req = new Dtos.RecommendationRequest("Prendre du repos");
        when(healthService.addRecommendation(eq(200L), anyString())).thenReturn(injuryDTO);

        mockMvc.perform(put("/medical/injuries/200/recommendation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("PUT /medical/records/{id} - Succès")
    void updateRecord_Success() throws Exception {
        when(healthService.updateRecord(eq(100L), any())).thenReturn(medicalRecordDTO);

        mockMvc.perform(put("/medical/records/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicalRecordDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight").value(75.5));
    }

    @Test
    @DisplayName("POST /medical/injuries - Succès")
    void declareInjury_Success() throws Exception {
        when(healthService.declareInjury(any())).thenReturn(injuryDTO);

        mockMvc.perform(post("/medical/injuries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(injuryDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.report").value("Douleur au genou"));
    }
}