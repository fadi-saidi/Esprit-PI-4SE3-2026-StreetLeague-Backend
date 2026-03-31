package tn.esprit.pi.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.OwnedPlayerResponse;
import tn.esprit.pi.repository.*;
import tn.esprit.pi.service.OwnedPlayerServiceImpl;
import tn.esprit.pi.config.VirtualTeamConfig;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnedPlayerServiceImplTest {

    @InjectMocks
    private OwnedPlayerServiceImpl service;

    @Mock
    private OwnedPlayerRepository ownedPlayerRepository;
    @Mock
    private VirtualTeamRepository teamRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PlayerProfileRepository playerRepository;

    private User user;
    private VirtualTeam team;
    private PlayerProfile playerProfile;
    private OwnedPlayer ownedPlayer;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setEnabled(true);

        team = new VirtualTeam();
        team.setId(1L);
        team.setSportType(SportType.FOOTBALL);

        playerProfile = new PlayerProfile();
        playerProfile.setId(1L);

        ownedPlayer = new OwnedPlayer();
        ownedPlayer.setId(1L);
        ownedPlayer.setUser(user);
        ownedPlayer.setVirtualTeam(team);
        ownedPlayer.setPlayerProfile(playerProfile);
        ownedPlayer.setStatus(PlayerStatus.TITULAIRE);
        ownedPlayer.setAcquiredDate(LocalDate.now());
    }

    // ------------------- Add player tests -------------------

    @Test
    void shouldAddPlayerAsStarter() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerProfile));
        when(ownedPlayerRepository.findByUserIdAndVirtualTeamId(1L, 1L)).thenReturn(Optional.empty());
        when(ownedPlayerRepository.countByVirtualTeamIdAndStatus(anyLong(), eq(PlayerStatus.TITULAIRE))).thenReturn(0L);
        when(ownedPlayerRepository.countByVirtualTeamIdAndStatus(anyLong(), eq(PlayerStatus.REMPLACANT))).thenReturn(0L);
        when(ownedPlayerRepository.save(any())).thenReturn(ownedPlayer);

        OwnedPlayerResponse response = service.addPlayerToTeam(1L, 1L, 1L);

        assertEquals(PlayerStatus.TITULAIRE, response.getStatus());
        assertEquals(user.getId(), response.getUserId());
        verify(ownedPlayerRepository, times(1)).save(any());
    }

    @Test
    void shouldAddPlayerAsSubstituteWhenStartersFull() {
        // Build a dedicated substitute player to return from save()
        OwnedPlayer substitutePlayer = new OwnedPlayer();
        substitutePlayer.setId(2L);
        substitutePlayer.setUser(user);
        substitutePlayer.setVirtualTeam(team);
        substitutePlayer.setPlayerProfile(playerProfile);
        substitutePlayer.setStatus(PlayerStatus.REMPLACANT);
        substitutePlayer.setAcquiredDate(LocalDate.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerProfile));
        when(ownedPlayerRepository.findByUserIdAndVirtualTeamId(1L, 1L)).thenReturn(Optional.empty());
        // Starters full
        when(ownedPlayerRepository.countByVirtualTeamIdAndStatus(anyLong(), eq(PlayerStatus.TITULAIRE)))
                .thenReturn(Long.valueOf(VirtualTeamConfig.MAX_STARTERS.get(team.getSportType())));
        // Substitutes available
        when(ownedPlayerRepository.countByVirtualTeamIdAndStatus(anyLong(), eq(PlayerStatus.REMPLACANT)))
                .thenReturn(0L);
        when(ownedPlayerRepository.save(any())).thenReturn(substitutePlayer);

        OwnedPlayerResponse response = service.addPlayerToTeam(1L, 1L, 1L);

        assertEquals(PlayerStatus.REMPLACANT, response.getStatus());
        verify(ownedPlayerRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowWhenTeamIsFull() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerProfile));
        when(ownedPlayerRepository.findByUserIdAndVirtualTeamId(1L, 1L)).thenReturn(Optional.empty());
        // Both starters and substitutes full
        when(ownedPlayerRepository.countByVirtualTeamIdAndStatus(anyLong(), eq(PlayerStatus.TITULAIRE)))
                .thenReturn(Long.valueOf(VirtualTeamConfig.MAX_STARTERS.get(team.getSportType())));
        when(ownedPlayerRepository.countByVirtualTeamIdAndStatus(anyLong(), eq(PlayerStatus.REMPLACANT)))
                .thenReturn(Long.valueOf(VirtualTeamConfig.MAX_SUBSTITUTES.get(team.getSportType())));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.addPlayerToTeam(1L, 1L, 1L));

        assertEquals("Cannot add more players, team is full", ex.getMessage());
    }

    @Test
    void shouldThrowIfPlayerAlreadyAssigned() {
        // Service checks user -> team -> player -> duplicate, so all must be mocked
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(playerProfile));
        when(ownedPlayerRepository.findByUserIdAndVirtualTeamId(1L, 1L))
                .thenReturn(Optional.of(ownedPlayer));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.addPlayerToTeam(1L, 1L, 1L));

        assertEquals("Player already assigned to this team", ex.getMessage());
    }

    @Test
    void shouldThrowIfUserSignedOut() {
        user.setEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.addPlayerToTeam(1L, 1L, 1L));

        assertEquals("User is inactive or signed out", ex.getMessage());
    }

    // ------------------- Remove player test -------------------

    @Test
    void shouldRemovePlayer() {
        when(ownedPlayerRepository.findById(1L)).thenReturn(Optional.of(ownedPlayer));

        service.removePlayerFromTeam(1L);

        verify(ownedPlayerRepository).delete(ownedPlayer);
    }

    // ------------------- Get players tests -------------------

    @Test
    void shouldReturnPlayersOfTeam() {
        when(ownedPlayerRepository.findByVirtualTeamId(1L)).thenReturn(List.of(ownedPlayer));

        List<OwnedPlayerResponse> players = service.getPlayersOfTeam(1L);

        assertEquals(1, players.size());
        assertEquals(1L, players.get(0).getId());
    }

    @Test
    void shouldReturnPlayersOfUser() {
        when(ownedPlayerRepository.findByUserId(1L)).thenReturn(List.of(ownedPlayer));

        List<OwnedPlayerResponse> players = service.getPlayersOfUser(1L);

        assertEquals(1, players.size());
        assertEquals(1L, players.get(0).getId());
    }

    @Test
    void shouldGetOwnedPlayerById() {
        when(ownedPlayerRepository.findById(1L)).thenReturn(Optional.of(ownedPlayer));

        OwnedPlayerResponse response = service.getOwnedPlayerById(1L);

        assertEquals(1L, response.getId());
        assertEquals(PlayerStatus.TITULAIRE, response.getStatus());
    }
}