package tn.esprit.pi.gestiontournoi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import tn.esprit.pi.domain.Role;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.DecisionRequest;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.MatchLookupResponse;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.MatchRequest;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.RefereeSummary;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.TeamSummary;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.UserSummary;
import tn.esprit.pi.gestiontournoi.entity.ApprovalStatus;
import tn.esprit.pi.gestiontournoi.entity.GestionApprovalEntity;
import tn.esprit.pi.gestiontournoi.entity.GestionMatch;
import tn.esprit.pi.gestiontournoi.repository.GestionMatchRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GestionMatchServiceTest {

    @Mock
    private GestionMatchRepository repository;
    @Mock
    private GestionModuleSupport support;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private GestionMatchService service;

    private User admin;
    private User requester;

    @BeforeEach
    void setUp() {
        admin = user(1L, "Admin", "admin@test.com", Role.ADMIN);
        requester = user(2L, "Requester", "request@test.com", Role.PLAYER);

        when(support.getEffectiveStatus(any(GestionApprovalEntity.class))).thenAnswer(invocation -> {
            GestionApprovalEntity entity = invocation.getArgument(0);
            return entity.getApprovalStatus() == null ? ApprovalStatus.APPROVED : entity.getApprovalStatus();
        });
        when(support.toUserSummary(any(), any(), any())).thenAnswer(invocation ->
                new UserSummary(invocation.getArgument(0), invocation.getArgument(1), invocation.getArgument(2), null));
        when(support.normalizeOptionalText(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            if (value == null) {
                return null;
            }
            String trimmed = value.trim();
            return trimmed.isEmpty() ? null : trimmed;
        });
        when(support.requireText(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation ->
                invocation.getArgument(0, String.class).trim());
        when(support.requireIsoDate(anyString(), anyString())).thenAnswer(invocation ->
                invocation.getArgument(0, String.class).trim());
        when(support.normalizeScore(nullable(String.class))).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            if (value == null) {
                return null;
            }
            String trimmed = value.trim();
            return trimmed.isEmpty() ? null : trimmed;
        });
        doAnswer(invocation -> {
            GestionMatch match = invocation.getArgument(0);
            User user = invocation.getArgument(1);
            match.setApprovalStatus(ApprovalStatus.PENDING);
            match.setRequesterUserId(user.getId());
            match.setRequesterName(user.getUsername());
            match.setRequesterEmail(user.getEmail());
            return null;
        }).when(support).markPendingRequest(any(GestionMatch.class), any(User.class));
        doAnswer(invocation -> {
            GestionMatch match = invocation.getArgument(0);
            ApprovalStatus status = invocation.getArgument(1);
            User user = invocation.getArgument(2);
            String note = invocation.getArgument(3);
            match.setApprovalStatus(status);
            match.setApprovedByUserId(user.getId());
            match.setApprovedByName(user.getUsername());
            match.setApprovedByEmail(user.getEmail());
            match.setApprovalNote(note);
            return null;
        }).when(support).markDecision(any(GestionMatch.class), any(ApprovalStatus.class), any(User.class), any());
        when(repository.save(any(GestionMatch.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(support.requireTeam(10L)).thenReturn(new TeamSummary(10L, "Street Wolves"));
        when(support.requireTeam(11L)).thenReturn(new TeamSummary(11L, "City Panthers"));
        when(support.requireReferee(8L)).thenReturn(new RefereeSummary(8L, "Ref Karim"));
    }

    @Test
    void getApproved_filtersPendingMatches() {
        when(support.getCurrentUser(authentication)).thenReturn(requester);
        when(repository.findAll()).thenReturn(List.of(match(1L, ApprovalStatus.PENDING), match(2L, ApprovalStatus.APPROVED)));

        var responses = service.getApproved(authentication);

        assertEquals(1, responses.size());
        assertEquals("Street Wolves", responses.get(0).homeTeam().name());
    }

    @Test
    void getRequests_andLookups_delegateToSupport() {
        when(support.requireAuthenticatedUser(authentication)).thenReturn(admin);
        when(repository.findAll()).thenReturn(List.of(match(3L, ApprovalStatus.PENDING)));
        when(support.getAllTeams()).thenReturn(List.of(new TeamSummary(10L, "Street Wolves")));
        when(support.getAllReferees()).thenReturn(List.of(new RefereeSummary(8L, "Ref Karim")));

        assertEquals(1, service.getRequests(authentication).size());
        MatchLookupResponse lookups = service.getLookups(authentication);
        assertEquals(1, lookups.teams().size());
        assertEquals(1, lookups.referees().size());
    }

    @Test
    void submit_update_approve_reject_andDelete_applyRules() {
        when(support.requireAuthenticatedUser(authentication)).thenReturn(requester, admin, admin, admin, admin);
        when(repository.findById(5L)).thenReturn(Optional.of(match(5L, ApprovalStatus.PENDING)));

        var submitted = service.submit(new MatchRequest(10L, 11L, 8L, "2026-05-10", " 2-1 ", " Arena "), authentication);
        assertEquals(ApprovalStatus.PENDING, submitted.approvalStatus());

        ArgumentCaptor<GestionMatch> captor = ArgumentCaptor.forClass(GestionMatch.class);
        verify(repository).save(captor.capture());
        assertEquals("Street Wolves", captor.getValue().getHomeTeamName());
        assertEquals("Ref Karim", captor.getValue().getRefereeName());

        assertEquals("Arena", service.update(5L, new MatchRequest(10L, 11L, 8L, "2026-05-12", "", "Arena"), authentication).location());
        assertEquals(ApprovalStatus.APPROVED, service.approve(5L, new DecisionRequest("ok"), authentication).approvalStatus());
        assertEquals(ApprovalStatus.REJECTED, service.reject(5L, new DecisionRequest("no"), authentication).approvalStatus());
        service.delete(5L, authentication);
        verify(repository).delete(any(GestionMatch.class));
    }

    @Test
    void approve_andReject_allowNullDecisionPayload() {
        when(support.requireAuthenticatedUser(authentication)).thenReturn(admin);
        when(repository.findById(15L)).thenReturn(Optional.of(match(15L, ApprovalStatus.PENDING)));

        assertEquals(ApprovalStatus.APPROVED, service.approve(15L, null, authentication).approvalStatus());
        assertEquals(ApprovalStatus.REJECTED, service.reject(15L, null, authentication).approvalStatus());
    }

    @Test
    void missingMatchThrows() {
        when(support.requireAuthenticatedUser(authentication)).thenReturn(admin);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.update(99L, new MatchRequest(10L, 11L, 8L, "2026-05-12", "", "Arena"), authentication));
    }

    @Test
    void submit_stillDelegatesDistinctTeamValidation() {
        when(support.requireAuthenticatedUser(authentication)).thenReturn(requester);
        doThrow(new IllegalArgumentException("A match must contain two different teams"))
                .when(support).validateDistinctTeams(10L, 10L);

        assertThrows(IllegalArgumentException.class,
                () -> service.submit(new MatchRequest(10L, 10L, 8L, "2026-05-10", "", "Arena"), authentication));
    }

    private GestionMatch match(Long id, ApprovalStatus status) {
        GestionMatch match = new GestionMatch();
        match.setId(id);
        match.setHomeTeamId(10L);
        match.setHomeTeamName("Street Wolves");
        match.setAwayTeamId(11L);
        match.setAwayTeamName("City Panthers");
        match.setRefereeUserId(8L);
        match.setRefereeName("Ref Karim");
        match.setDate("2026-05-01");
        match.setScore("1-0");
        match.setLocation("Arena");
        match.setApprovalStatus(status);
        return match;
    }

    private User user(Long id, String username, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }
}
