package tn.esprit.pi.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.VirtualTeamDto;
import tn.esprit.pi.dto.VirtualTeamResponse;
import tn.esprit.pi.repository.*;
import tn.esprit.pi.service.VirtualTeamServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VirtualTeamServiceImplTest {

    @InjectMocks
    private VirtualTeamServiceImpl service;

    @Mock
    private VirtualTeamRepository teamRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PredictionRepository predictionRepository;

    private User user;
    private VirtualTeam team;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);

        team = new VirtualTeam();
        team.setId(1L);
        team.setUser(user);
        team.setName("Dream Team");
        team.setSportType(SportType.FOOTBALL);
    }

    @Test
    void shouldCreateVirtualTeam() {
        VirtualTeamDto dto = new VirtualTeamDto();
        dto.setName("Dream Team");
        dto.setSportType(SportType.FOOTBALL);
        dto.setUserId(1L);
        dto.setPlayerIds(List.of(10L, 20L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(teamRepository.save(any())).thenReturn(team);

        VirtualTeamResponse response = service.createVirtualTeam(dto);

        assertEquals("Dream Team", response.getName());
        assertEquals(1L, response.getUserId());
        assertEquals(SportType.FOOTBALL, response.getSportType());
        verify(teamRepository).save(any());
    }

    @Test
    void shouldUpdateVirtualTeam() {
        VirtualTeamDto dto = new VirtualTeamDto();
        dto.setName("Updated");
        dto.setSportType(SportType.BASKETBALL);
        dto.setPlayerIds(List.of(30L));

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(teamRepository.save(any())).thenReturn(team);

        VirtualTeamResponse response = service.updateVirtualTeam(1L, dto);

        assertEquals("Updated", response.getName());
        assertEquals(SportType.BASKETBALL, response.getSportType());
    }

    @Test
    void shouldDeleteVirtualTeam() {
        doNothing().when(predictionRepository).deleteByVirtualTeamId(1L);
        doNothing().when(teamRepository).deleteById(1L);

        service.deleteVirtualTeam(1L);

        verify(predictionRepository).deleteByVirtualTeamId(1L);
        verify(teamRepository).deleteById(1L);
    }

    @Test
    void shouldGetAllTeams() {
        when(teamRepository.findAll()).thenReturn(List.of(team));

        List<VirtualTeamResponse> teams = service.getAllTeams();

        assertEquals(1, teams.size());
        assertEquals("Dream Team", teams.get(0).getName());
    }

    @Test
    void shouldGetTeamsByUser() {
        when(teamRepository.findByUserId(1L)).thenReturn(List.of(team));

        List<VirtualTeamResponse> teams = service.getTeamsByUser(1L);

        assertEquals(1, teams.size());
        assertEquals(1L, teams.get(0).getUserId());
    }
}