package tn.esprit.pi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.esprit.pi.controller.PredictionController;
import tn.esprit.pi.domain.PredictionStatus;
import tn.esprit.pi.dto.PlayerStatDto;
import tn.esprit.pi.dto.PredictionDto;
import tn.esprit.pi.dto.PredictionResponse;
import tn.esprit.pi.service.IPredictionService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PredictionControllerTest {

    @Mock
    private IPredictionService predictionService;

    @InjectMocks
    private PredictionController predictionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(predictionController)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // needed for LocalDate serialization
    }

    private PredictionResponse buildResponse(Long id, Long virtualTeamId) {
        return PredictionResponse.builder()
                .id(id)
                .virtualTeamId(virtualTeamId)
                .weekNumber(1)
                .weekYear(2024)
                .captainPlayerId(1L)
                .status(PredictionStatus.PENDING)
                .totalPointsEarned(0.0)
                .createdAt(LocalDate.of(2024, 1, 1))
                .playerPredictions(List.of())
                .build();
    }

    @Test
    void shouldSubmitPrediction() throws Exception {
        PredictionDto dto = PredictionDto.builder()
                .virtualTeamId(1L)
                .captainPlayerId(1L)
                .players(List.of())
                .build();

        PredictionResponse response = buildResponse(1L, 1L);
        when(predictionService.submitPrediction(any(PredictionDto.class))).thenReturn(response);

        mockMvc.perform(post("/predictions/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.virtualTeamId").value(1L))
                .andExpect(jsonPath("$.weekNumber").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(predictionService, times(1)).submitPrediction(any(PredictionDto.class));
    }

    @Test
    void shouldGetCurrentPrediction() throws Exception {
        PredictionResponse response = buildResponse(1L, 1L);
        when(predictionService.getCurrentPrediction(1L)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/predictions/current/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.virtualTeamId").value(1L));

        verify(predictionService, times(1)).getCurrentPrediction(1L);
    }

    @Test
    void shouldReturnEmptyWhenNoCurrentPrediction() throws Exception {
        when(predictionService.getCurrentPrediction(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/predictions/current/99"))
                .andExpect(status().isOk());

        verify(predictionService, times(1)).getCurrentPrediction(99L);
    }

    @Test
    void shouldGetPredictionHistory() throws Exception {
        List<PredictionResponse> history = List.of(
                buildResponse(1L, 1L),
                buildResponse(2L, 1L)
        );
        when(predictionService.getPredictionHistory(1L)).thenReturn(history);

        mockMvc.perform(get("/predictions/history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(predictionService, times(1)).getPredictionHistory(1L);
    }

    @Test
    void shouldSavePlayerStats() throws Exception {
        PlayerStatDto dto = PlayerStatDto.builder()
                .playerId(1L)
                .playerName("John Doe")
                .sportType("FOOTBALL")
                .weekNumber(1)
                .weekYear(2024)
                .goalsScored(2)
                .yellowCards(0)
                .redCards(0)
                .basketballPoints(0)
                .tennisWin(false)
                .build();

        doNothing().when(predictionService).savePlayerStat(any(PlayerStatDto.class));

        mockMvc.perform(post("/predictions/admin/stats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(predictionService, times(1)).savePlayerStat(any(PlayerStatDto.class));
    }

    @Test
    void shouldResolvePrediction() throws Exception {
        doNothing().when(predictionService).resolvePredictionById(1L);

        mockMvc.perform(post("/predictions/admin/resolve/1"))
                .andExpect(status().isOk());

        verify(predictionService, times(1)).resolvePredictionById(eq(1L));
    }
}