package tn.esprit.pi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.JoinRequestDTO;
import tn.esprit.pi.dto.PlayerSummaryDTO;
import tn.esprit.pi.dto.TeamDTO;
import tn.esprit.pi.repository.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest {

    @Mock private TeamRepository         teamRepository;
    @Mock private UserRepository         userRepository;
    @Mock private PlayerProfileRepository playerProfileRepository;
    @Mock private JoinRequestRepository  joinRequestRepository;

    @InjectMocks private TeamServiceImpl teamService;

    private Team     team;
    private User     captain;
    private PlayerProfile captainProfile;

    @BeforeEach
    void setUp() {
        captain = new User();
        captain.setId(1L);
        captain.setUsername("alice");
        captain.setEmail("alice@test.com");
        captain.setRole(Role.PLAYER);

        captainProfile = new PlayerProfile();
        captainProfile.setId(1L);
        captainProfile.setUser(captain);
        captainProfile.setTeams(new HashSet<>());

        team = new Team();
        team.setId(10L);
        team.setName("Thunder Hawks");
        team.setSportType(SportType.FOOTBALL);
        team.setCaptainId(1L);
        team.setPlayerProfiles(new HashSet<>(Set.of(captainProfile)));
        team.setCreatedAt(java.time.LocalDateTime.now());
    }

    // ── createTeam ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createTeam: saves team and auto-adds captain as player")
    void createTeam_success() {
        TeamDTO dto = new TeamDTO();
        dto.setName("Thunder Hawks");
        dto.setType("FOOTBALL");
        dto.setCaptainId(1L);

        when(teamRepository.save(any(Team.class))).thenReturn(team);
        when(playerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(captainProfile));
        when(userRepository.findById(1L)).thenReturn(Optional.of(captain));

        TeamDTO result = teamService.createTeam(dto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Thunder Hawks");
        assertThat(result.getType()).isEqualTo("FOOTBALL");
        verify(teamRepository, atLeastOnce()).save(any(Team.class));
    }

    @Test
    @DisplayName("createTeam: captain not a player — team still saved")
    void createTeam_captainWithoutProfile() {
        TeamDTO dto = new TeamDTO();
        dto.setName("Storm FC");
        dto.setType("BASKETBALL");
        dto.setCaptainId(99L);

        Team saved = new Team();
        saved.setId(11L);
        saved.setName("Storm FC");
        saved.setSportType(SportType.BASKETBALL);
        saved.setCaptainId(99L);

        when(teamRepository.save(any(Team.class))).thenReturn(saved);
        when(playerProfileRepository.findByUserId(99L)).thenReturn(Optional.empty());

        TeamDTO result = teamService.createTeam(dto);

        assertThat(result.getName()).isEqualTo("Storm FC");
        verify(teamRepository, times(1)).save(any(Team.class));
    }

    // ── getAllTeams ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllTeams: returns list of TeamDTOs")
    void getAllTeams_returnsList() {
        when(teamRepository.findAll()).thenReturn(List.of(team));
        when(userRepository.findById(1L)).thenReturn(Optional.of(captain));

        List<TeamDTO> result = teamService.getAllTeams();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Thunder Hawks");
        assertThat(result.get(0).getPlayerCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("getAllTeams: empty repository returns empty list")
    void getAllTeams_empty() {
        when(teamRepository.findAll()).thenReturn(List.of());

        List<TeamDTO> result = teamService.getAllTeams();

        assertThat(result).isEmpty();
    }

    // ── getTeamById ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getTeamById: existing id returns DTO")
    void getTeamById_found() {
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findById(1L)).thenReturn(Optional.of(captain));

        TeamDTO result = teamService.getTeamById(10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getType()).isEqualTo("FOOTBALL");
    }

    @Test
    @DisplayName("getTeamById: unknown id throws RuntimeException")
    void getTeamById_notFound() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.getTeamById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    // ── updateTeam ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateTeam: name and type updated")
    void updateTeam_success() {
        TeamDTO dto = new TeamDTO();
        dto.setName("New Name");
        dto.setType("BASKETBALL");

        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(teamRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(1L)).thenReturn(Optional.of(captain));

        TeamDTO result = teamService.updateTeam(10L, dto);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getType()).isEqualTo("BASKETBALL");
    }

    // ── deleteTeam ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteTeam: clears players and deletes team")
    void deleteTeam_success() {
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        doNothing().when(joinRequestRepository).deleteByTeam(team);

        teamService.deleteTeam(10L);

        assertThat(team.getPlayerProfiles()).isEmpty();
        verify(teamRepository).delete(team);
    }

    // ── requestJoin ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("requestJoin: creates PENDING REQUEST")
    void requestJoin_success() {
        User player2 = new User(); player2.setId(2L);
        player2.setUsername("bob"); player2.setEmail("bob@test.com");
        PlayerProfile p2 = new PlayerProfile(); p2.setId(2L); p2.setUser(player2); p2.setTeams(new HashSet<>());

        JoinRequest jr = JoinRequest.builder().id(1L).team(team).player(player2)
                .type("REQUEST").status("PENDING").build();

        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findById(2L)).thenReturn(Optional.of(player2));
        when(playerProfileRepository.findByUserId(2L)).thenReturn(Optional.of(p2));
        when(joinRequestRepository.existsByTeamAndPlayerAndStatusAndType(any(), any(), any(), any())).thenReturn(false);
        when(joinRequestRepository.save(any())).thenReturn(jr);

        JoinRequestDTO result = teamService.requestJoin(10L, 2L);

        assertThat(result.getType()).isEqualTo("REQUEST");
        assertThat(result.getStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("requestJoin: duplicate throws RuntimeException")
    void requestJoin_duplicate() {
        User player2 = new User(); player2.setId(2L);
        PlayerProfile p2 = new PlayerProfile(); p2.setId(2L); p2.setUser(player2); p2.setTeams(new HashSet<>());

        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findById(2L)).thenReturn(Optional.of(player2));
        when(playerProfileRepository.findByUserId(2L)).thenReturn(Optional.of(p2));
        when(joinRequestRepository.existsByTeamAndPlayerAndStatusAndType(any(), any(), eq("PENDING"), eq("REQUEST"))).thenReturn(true);

        assertThatThrownBy(() -> teamService.requestJoin(10L, 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already sent");
    }

    // ── acceptRequest ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("acceptRequest: sets ACCEPTED and adds player to team")
    void acceptRequest_addsPlayer() {
        User player2 = new User(); player2.setId(2L);
        player2.setUsername("bob");
        PlayerProfile p2 = new PlayerProfile(); p2.setId(2L); p2.setUser(player2);

        JoinRequest jr = JoinRequest.builder().id(5L).team(team).player(player2)
                .type("REQUEST").status("PENDING").build();

        when(joinRequestRepository.findById(5L)).thenReturn(Optional.of(jr));
        when(playerProfileRepository.findByUserId(2L)).thenReturn(Optional.of(p2));
        when(teamRepository.save(any())).thenReturn(team);

        teamService.acceptRequest(5L);

        assertThat(jr.getStatus()).isEqualTo("ACCEPTED");
        assertThat(team.getPlayerProfiles()).contains(p2);
    }

    // ── getAllPlayers ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllPlayers: returns only PLAYER role users")
    void getAllPlayers_onlyPlayers() {
        User admin = new User(); admin.setId(99L); admin.setRole(Role.ADMIN);
        when(userRepository.findAll()).thenReturn(List.of(captain, admin));

        List<PlayerSummaryDTO> result = teamService.getAllPlayers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFullName()).isEqualTo("alice");
    }

    // ── removePlayer ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("removePlayer: removes profile from team")
    void removePlayer_success() {
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(playerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(captainProfile));
        when(teamRepository.save(any())).thenReturn(team);

        teamService.removePlayer(10L, 1L);

        assertThat(team.getPlayerProfiles()).doesNotContain(captainProfile);
    }
}
