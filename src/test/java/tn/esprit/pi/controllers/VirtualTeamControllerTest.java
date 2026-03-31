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
import tn.esprit.pi.controller.VirtualTeamController;
import tn.esprit.pi.domain.SportType;
import tn.esprit.pi.dto.VirtualTeamDto;
import tn.esprit.pi.dto.VirtualTeamResponse;
import tn.esprit.pi.service.IVirtualTeamService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class VirtualTeamControllerTest {

    @Mock
    private IVirtualTeamService teamService;

    @InjectMocks
    private VirtualTeamController virtualTeamController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        // Standalone setup — no Spring context, no Security, no @WebMvcTest needed
        mockMvc = MockMvcBuilders
                .standaloneSetup(virtualTeamController)
                .build();
        objectMapper = new ObjectMapper();
    }

    private VirtualTeamResponse buildResponse(Long id, String name) {
        return VirtualTeamResponse.builder()
                .id(id)
                .name(name)
                .sportType(SportType.FOOTBALL)
                .earnedPoints(0.0)
                .weekPoints(0.0)
                .userId(1L)
                .playerIds(List.of())
                .build();
    }

    private VirtualTeamDto buildDto(String name) {
        VirtualTeamDto dto = new VirtualTeamDto();
        dto.setName(name);
        dto.setSportType(SportType.FOOTBALL);
        dto.setUserId(1L);
        dto.setPlayerIds(List.of());
        dto.setEarnedPoints(0.0);
        dto.setWeekPoints(0.0);
        return dto;
    }

    @Test
    void shouldCreateTeam() throws Exception {
        VirtualTeamDto dto = buildDto("Team Alpha");
        VirtualTeamResponse response = buildResponse(1L, "Team Alpha");
        when(teamService.createVirtualTeam(any(VirtualTeamDto.class))).thenReturn(response);

        mockMvc.perform(post("/virtual-teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Team Alpha"));

        verify(teamService, times(1)).createVirtualTeam(any(VirtualTeamDto.class));
    }

    @Test
    void shouldUpdateTeam() throws Exception {
        VirtualTeamDto dto = buildDto("Updated Team");
        VirtualTeamResponse response = buildResponse(1L, "Updated Team");
        when(teamService.updateVirtualTeam(eq(1L), any(VirtualTeamDto.class))).thenReturn(response);

        mockMvc.perform(put("/virtual-teams/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Team"));

        verify(teamService, times(1)).updateVirtualTeam(eq(1L), any(VirtualTeamDto.class));
    }

    @Test
    void shouldDeleteTeam() throws Exception {
        doNothing().when(teamService).deleteVirtualTeam(1L);

        mockMvc.perform(delete("/virtual-teams/1"))
                .andExpect(status().isOk());

        verify(teamService, times(1)).deleteVirtualTeam(1L);
    }

    @Test
    void shouldGetTeamById() throws Exception {
        VirtualTeamResponse response = buildResponse(1L, "Team Alpha");
        when(teamService.getVirtualTeamById(1L)).thenReturn(response);

        mockMvc.perform(get("/virtual-teams/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Team Alpha"));

        verify(teamService, times(1)).getVirtualTeamById(1L);
    }

    @Test
    void shouldGetAllTeams() throws Exception {
        List<VirtualTeamResponse> teams = List.of(
                buildResponse(1L, "Team A"),
                buildResponse(2L, "Team B")
        );
        when(teamService.getAllTeams()).thenReturn(teams);

        mockMvc.perform(get("/virtual-teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Team A"))
                .andExpect(jsonPath("$[1].name").value("Team B"));

        verify(teamService, times(1)).getAllTeams();
    }

    @Test
    void shouldGetTeamsByUser() throws Exception {
        List<VirtualTeamResponse> teams = List.of(buildResponse(1L, "Team A"));
        when(teamService.getTeamsByUser(1L)).thenReturn(teams);

        mockMvc.perform(get("/virtual-teams/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(teamService, times(1)).getTeamsByUser(1L);
    }
}