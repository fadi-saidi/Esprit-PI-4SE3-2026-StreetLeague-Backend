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
import tn.esprit.pi.controller.OwnedPlayerController;
import tn.esprit.pi.domain.PlayerStatus;
import tn.esprit.pi.dto.OwnedPlayerResponse;
import tn.esprit.pi.service.IOwnedPlayerService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OwnedPlayerControllerTest {

    @Mock
    private IOwnedPlayerService ownedPlayerService;

    @InjectMocks
    private OwnedPlayerController ownedPlayerController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(ownedPlayerController)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // needed for LocalDate serialization
    }

    private OwnedPlayerResponse buildResponse(Long id, Long userId, Long teamId) {
        return OwnedPlayerResponse.builder()
                .id(id)
                .userId(userId)
                .teamId(teamId)
                .playerProfileId(1L)
                .status(PlayerStatus.TITULAIRE)
                .acquiredDate(LocalDate.of(2024, 1, 1))
                .build();
    }

    @Test
    void shouldAddPlayerToTeam() throws Exception {
        OwnedPlayerResponse response = buildResponse(1L, 1L, 1L);
        when(ownedPlayerService.addPlayerToTeam(1L, 1L, 1L)).thenReturn(response);

        mockMvc.perform(post("/owned-players/add")
                        .param("userId", "1")
                        .param("teamId", "1")
                        .param("playerProfileId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.teamId").value(1L))
                .andExpect(jsonPath("$.playerProfileId").value(1L))
                .andExpect(jsonPath("$.status").value("TITULAIRE"));

        verify(ownedPlayerService, times(1)).addPlayerToTeam(1L, 1L, 1L);
    }

    @Test
    void shouldRemovePlayerFromTeam() throws Exception {
        doNothing().when(ownedPlayerService).removePlayerFromTeam(1L);

        mockMvc.perform(delete("/owned-players/remove/1"))
                .andExpect(status().isOk());

        verify(ownedPlayerService, times(1)).removePlayerFromTeam(1L);
    }

    @Test
    void shouldGetPlayersOfTeam() throws Exception {
        List<OwnedPlayerResponse> players = List.of(
                buildResponse(1L, 1L, 1L),
                buildResponse(2L, 2L, 1L)
        );
        when(ownedPlayerService.getPlayersOfTeam(1L)).thenReturn(players);

        mockMvc.perform(get("/owned-players/team/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(ownedPlayerService, times(1)).getPlayersOfTeam(1L);
    }

    @Test
    void shouldGetPlayersOfUser() throws Exception {
        List<OwnedPlayerResponse> players = List.of(
                buildResponse(1L, 1L, 1L)
        );
        when(ownedPlayerService.getPlayersOfUser(1L)).thenReturn(players);

        mockMvc.perform(get("/owned-players/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(1L));

        verify(ownedPlayerService, times(1)).getPlayersOfUser(1L);
    }

    @Test
    void shouldGetOwnedPlayerById() throws Exception {
        OwnedPlayerResponse response = buildResponse(1L, 1L, 1L);
        when(ownedPlayerService.getOwnedPlayerById(1L)).thenReturn(response);

        mockMvc.perform(get("/owned-players/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("TITULAIRE"));

        verify(ownedPlayerService, times(1)).getOwnedPlayerById(1L);
    }
}