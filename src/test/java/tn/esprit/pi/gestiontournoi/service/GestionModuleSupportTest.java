package tn.esprit.pi.gestiontournoi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import tn.esprit.pi.domain.CoachProfile;
import tn.esprit.pi.domain.PlayerProfile;
import tn.esprit.pi.domain.RefereeProfile;
import tn.esprit.pi.domain.Role;
import tn.esprit.pi.domain.Team;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.PlayerSummary;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.RefereeSummary;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.TeamSummary;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.UserSummary;
import tn.esprit.pi.gestiontournoi.entity.ApprovalStatus;
import tn.esprit.pi.gestiontournoi.entity.GestionEvent;
import tn.esprit.pi.repository.CoachProfileRepository;
import tn.esprit.pi.repository.PlayerProfileRepository;
import tn.esprit.pi.repository.RefereeProfileRepository;
import tn.esprit.pi.repository.TeamRepository;
import tn.esprit.pi.repository.UserRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestionModuleSupportTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private RefereeProfileRepository refereeProfileRepository;
    @Mock
    private CoachProfileRepository coachProfileRepository;
    @Mock
    private PlayerProfileRepository playerProfileRepository;

    @InjectMocks
    private GestionModuleSupport support;

    private Authentication authentication;

    @BeforeEach
    void setUp() {
        authentication = mock(Authentication.class);
    }

    @Test
    void getCurrentUser_returnsNullForAnonymousOrMissingUser() {
        assertNull(support.getCurrentUser(null));

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("anonymousUser");

        assertNull(support.getCurrentUser(authentication));

        when(authentication.getName()).thenReturn(null);
        assertNull(support.getCurrentUser(authentication));

        when(authentication.getName()).thenReturn("   ");
        assertNull(support.getCurrentUser(authentication));

        when(authentication.getName()).thenReturn("missing@test.com");
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertNull(support.getCurrentUser(authentication));
    }

    @Test
    void getCurrentUser_returnsUserForAuthenticatedEmail() {
        User user = user(7L, "coach", "coach@test.com", Role.COACH);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("coach@test.com");
        when(userRepository.findByEmail("coach@test.com")).thenReturn(Optional.of(user));

        assertEquals(user, support.getCurrentUser(authentication));
    }

    @Test
    void requireAuthenticatedUser_andRequireAdmin_applyRoleChecks() {
        when(authentication.isAuthenticated()).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> support.requireAuthenticatedUser(authentication));

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin@test.com");
        User admin = user(1L, "admin", "admin@test.com", Role.ADMIN);
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        assertEquals(admin, support.requireAuthenticatedUser(authentication));

        User player = user(2L, "player", "player@test.com", Role.PLAYER);
        assertThrows(AccessDeniedException.class, () -> support.requireAdmin(player));

        assertDoesNotThrow(() -> support.requireAdmin(admin));
    }

    @Test
    void requireCoachProfile_requiresCoachAndReturnsProfile() {
        User coachUser = user(3L, "coach", "coach@test.com", Role.COACH);
        CoachProfile coachProfile = coachProfile(coachUser);
        when(coachProfileRepository.findByUserId(3L)).thenReturn(Optional.of(coachProfile));

        assertEquals(coachProfile, support.requireCoachProfile(coachUser));
        assertEquals(coachProfile, support.requireCoachProfileByUserId(3L));

        User player = user(4L, "player", "player@test.com", Role.PLAYER);
        assertThrows(AccessDeniedException.class, () -> support.requireCoachProfile(player));
    }

    @Test
    void requireTeam_andRequireReferee_returnSummaries() {
        Team team = new Team();
        team.setId(10L);
        team.setName("Street Wolves");
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));

        User refereeUser = user(8L, "Ref Karim", "ref@test.com", Role.REFEREE);
        RefereeProfile refereeProfile = refereeProfile(refereeUser);
        when(refereeProfileRepository.findByUserId(8L)).thenReturn(Optional.of(refereeProfile));

        TeamSummary teamSummary = support.requireTeam(10L);
        RefereeSummary refereeSummary = support.requireReferee(8L);

        assertEquals("Street Wolves", teamSummary.name());
        assertEquals("Ref Karim", refereeSummary.name());
    }

    @Test
    void requirePlayers_preservesOrderAndThrowsForMissingPlayer() {
        PlayerProfile first = playerProfile(user(5L, "Player One", "p1@test.com", Role.PLAYER));
        PlayerProfile second = playerProfile(user(6L, "Player Two", "p2@test.com", Role.PLAYER));
        when(playerProfileRepository.findByUserIdIn(List.of(6L, 5L))).thenReturn(List.of(first, second));

        List<PlayerSummary> summaries = support.requirePlayers(List.of(6L, 5L));
        assertEquals(List.of(6L, 5L), summaries.stream().map(PlayerSummary::userId).toList());

        when(playerProfileRepository.findByUserIdIn(List.of(7L))).thenReturn(List.of());
        assertThrows(IllegalArgumentException.class, () -> support.requirePlayers(List.of(7L)));
    }

    @Test
    void requirePlayers_returnsEmptyForNullOrEmptyInput() {
        assertEquals(List.of(), support.requirePlayers(null));
        assertEquals(List.of(), support.requirePlayers(java.util.Arrays.asList(null, null)));
    }

    @Test
    void coachLookupHelpers_returnSortedPlayersAndIds() {
        CoachProfile coachProfile = new CoachProfile();
        coachProfile.setId(11L);
        PlayerProfile second = playerProfile(user(9L, "Zed", "zed@test.com", Role.PLAYER));
        PlayerProfile first = playerProfile(user(8L, "Adam", "adam@test.com", Role.PLAYER));

        when(playerProfileRepository.findDistinctByTeams_CoachProfile_Id(11L)).thenReturn(List.of(second, first));

        List<PlayerSummary> players = support.getPlayersForCoach(coachProfile);
        Set<Long> ids = support.getAllowedPlayerIdsForCoach(coachProfile);

        assertEquals(List.of("Adam", "Zed"), players.stream().map(PlayerSummary::name).toList());
        assertEquals(Set.of(8L, 9L), ids);
    }

    @Test
    void globalLookups_areSortedAndUserSummaryCanFallback() {
        Team secondTeam = new Team();
        secondTeam.setId(2L);
        secondTeam.setName("Zeta");
        Team firstTeam = new Team();
        firstTeam.setId(1L);
        firstTeam.setName("Alpha");
        when(teamRepository.findAll()).thenReturn(List.of(secondTeam, firstTeam));

        RefereeProfile secondRef = refereeProfile(user(3L, "Ref Z", "refz@test.com", Role.REFEREE));
        RefereeProfile firstRef = refereeProfile(user(2L, "Ref A", "refa@test.com", Role.REFEREE));
        when(refereeProfileRepository.findAll()).thenReturn(List.of(secondRef, firstRef));

        when(userRepository.findById(15L)).thenReturn(Optional.empty());

        assertEquals(List.of("Alpha", "Zeta"), support.getAllTeams().stream().map(TeamSummary::name).toList());
        assertEquals(List.of("Ref A", "Ref Z"), support.getAllReferees().stream().map(RefereeSummary::name).toList());

        UserSummary fallback = support.toUserSummary(15L, "Fallback", "fallback@test.com");
        assertEquals("Fallback", fallback.name());
    }

    @Test
    void toUserSummary_handlesBlankFallbackNullIdsAndResolvedUsers() {
        assertNull(support.toUserSummary(null, null, null));
        assertNull(support.toUserSummary(null, "   ", "   "));

        UserSummary noIdFallback = support.toUserSummary(null, "Guest", "guest@test.com");
        assertNull(noIdFallback.id());
        assertEquals("Guest", noIdFallback.name());
        assertEquals("guest@test.com", noIdFallback.email());
        assertNull(noIdFallback.role());

        User resolved = user(30L, "Resolved", "resolved@test.com", Role.COACH);
        when(userRepository.findById(30L)).thenReturn(Optional.of(resolved));

        UserSummary resolvedSummary = support.toUserSummary(30L, "Ignored", "ignored@test.com");
        assertEquals(30L, resolvedSummary.id());
        assertEquals("Resolved", resolvedSummary.name());
        assertEquals("resolved@test.com", resolvedSummary.email());
        assertEquals("COACH", resolvedSummary.role());
    }

    @Test
    void helperMethods_normalize_and_validate_and_markStates() {
        GestionEvent event = new GestionEvent();
        User requester = user(20L, "Requester", "request@test.com", Role.PLAYER);
        User admin = user(1L, "Admin", "admin@test.com", Role.ADMIN);

        assertNull(support.normalizeOptionalText(null));
        assertNull(support.normalizeOptionalText("   "));
        assertEquals("hello", support.normalizeOptionalText(" hello "));
        assertEquals("desc", support.normalizeOptionalTextWithLimit(" desc ", 10, "La description"));
        assertThrows(IllegalArgumentException.class,
                () -> support.normalizeOptionalTextWithLimit("0123456789ABC", 5, "La description"));

        assertEquals("Event", support.requireText(" Event ", "Le nom", 3, 10));
        assertThrows(IllegalArgumentException.class, () -> support.requireText("  ", "Le nom", 3, 10));
        assertThrows(IllegalArgumentException.class, () -> support.requireText("ab", "Le nom", 3, 10));
        assertThrows(IllegalArgumentException.class, () -> support.requireText("0123456789ABC", "Le nom", 3, 10));

        assertEquals("2026-04-01", support.requireIsoDate("2026-04-01", "La date"));
        assertThrows(IllegalArgumentException.class, () -> support.requireIsoDate("2026-02-30", "La date"));
        assertEquals(60, support.requireInteger(60, "La duree", 1, 480));
        assertThrows(IllegalArgumentException.class, () -> support.requireInteger(0, "La duree", 1, 480));

        assertEquals("2 - 1", support.normalizeScore("2-1"));
        assertNull(support.normalizeScore("   "));
        assertThrows(IllegalArgumentException.class, () -> support.normalizeScore("2/1"));
        assertEquals(new LinkedHashSet<>(List.of(5L, 6L)),
                support.requireSelectedIds(List.of(5L, 5L, 6L), "Les joueurs"));
        assertThrows(IllegalArgumentException.class, () -> support.requireSelectedIds(java.util.Arrays.asList(5L, null), "Les joueurs"));
        assertThrows(IllegalArgumentException.class, () -> support.requireSelectedIds(List.of(), "Les joueurs"));

        assertDoesNotThrow(() -> support.validateDateRange("2026-04-01", "2026-04-02"));
        assertThrows(IllegalArgumentException.class, () -> support.validateDateRange("2026-02-30", "2026-04-02"));
        assertThrows(IllegalArgumentException.class, () -> support.validateDateRange("2026-04-03", "2026-04-02"));
        assertDoesNotThrow(() -> support.validateDistinctTeams(1L, 2L));
        assertThrows(IllegalArgumentException.class, () -> support.validateDistinctTeams(3L, 3L));

        event.setApprovalStatus(null);
        assertEquals(ApprovalStatus.APPROVED, support.getEffectiveStatus(event));
        support.markPendingRequest(event, requester);
        assertEquals(ApprovalStatus.PENDING, support.getEffectiveStatus(event));
        assertEquals(20L, event.getRequesterUserId());

        support.markDecision(event, ApprovalStatus.APPROVED, admin, "  ok  ");
        assertEquals(ApprovalStatus.APPROVED, support.getEffectiveStatus(event));
        assertEquals(1L, event.getApprovedByUserId());
        assertEquals("ok", event.getApprovalNote());
    }

    private User user(Long id, String username, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    private CoachProfile coachProfile(User user) {
        CoachProfile profile = new CoachProfile();
        profile.setId(user.getId());
        profile.setUser(user);
        return profile;
    }

    private RefereeProfile refereeProfile(User user) {
        RefereeProfile profile = new RefereeProfile();
        profile.setId(user.getId());
        profile.setUser(user);
        return profile;
    }

    private PlayerProfile playerProfile(User user) {
        PlayerProfile profile = new PlayerProfile();
        profile.setId(user.getId());
        profile.setUser(user);
        return profile;
    }
}
