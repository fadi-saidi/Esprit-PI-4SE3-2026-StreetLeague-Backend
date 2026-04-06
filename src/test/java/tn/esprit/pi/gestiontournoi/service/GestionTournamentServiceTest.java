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
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.TournamentRequest;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.UserSummary;
import tn.esprit.pi.gestiontournoi.entity.ApprovalStatus;
import tn.esprit.pi.gestiontournoi.entity.GestionApprovalEntity;
import tn.esprit.pi.gestiontournoi.entity.GestionTournament;
import tn.esprit.pi.gestiontournoi.repository.GestionTournamentRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GestionTournamentServiceTest {

    @Mock
    private GestionTournamentRepository repository;
    @Mock
    private GestionModuleSupport support;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private GestionTournamentService service;

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
        when(support.requireText(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation ->
                invocation.getArgument(0, String.class).trim());
        when(support.requireIsoDate(anyString(), anyString())).thenAnswer(invocation ->
                invocation.getArgument(0, String.class).trim());
        when(support.requireInteger(any(), anyString(), anyInt(), anyInt())).thenAnswer(invocation ->
                invocation.getArgument(0, Integer.class));
        when(support.toUserSummary(any(), any(), any())).thenAnswer(invocation ->
                new UserSummary(invocation.getArgument(0), invocation.getArgument(1), invocation.getArgument(2), null));
        doAnswer(invocation -> {
            GestionTournament tournament = invocation.getArgument(0);
            User user = invocation.getArgument(1);
            tournament.setApprovalStatus(ApprovalStatus.PENDING);
            tournament.setRequesterUserId(user.getId());
            tournament.setRequesterName(user.getUsername());
            tournament.setRequesterEmail(user.getEmail());
            return null;
        }).when(support).markPendingRequest(any(GestionTournament.class), any(User.class));
        doAnswer(invocation -> {
            GestionTournament tournament = invocation.getArgument(0);
            ApprovalStatus status = invocation.getArgument(1);
            User user = invocation.getArgument(2);
            String note = invocation.getArgument(3);
            tournament.setApprovalStatus(status);
            tournament.setApprovedByUserId(user.getId());
            tournament.setApprovedByName(user.getUsername());
            tournament.setApprovedByEmail(user.getEmail());
            tournament.setApprovalNote(note);
            return null;
        }).when(support).markDecision(any(GestionTournament.class), any(ApprovalStatus.class), any(User.class), any());
        when(repository.save(any(GestionTournament.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void getApproved_filtersPendingTournamentsAndMarksParticipation() {
        when(support.getCurrentUser(authentication)).thenReturn(requester);
        GestionTournament approved = tournament(1L, "Cup", ApprovalStatus.APPROVED);
        approved.setParticipantUserIds(new LinkedHashSet<>(List.of(2L)));
        GestionTournament pending = tournament(2L, "Pending", ApprovalStatus.PENDING);
        when(repository.findAll()).thenReturn(List.of(pending, approved));

        var responses = service.getApproved(authentication);

        assertEquals(1, responses.size());
        assertTrue(responses.get(0).participating());
    }

    @Test
    void getApproved_handlesAnonymousUserWithoutParticipation() {
        when(support.getCurrentUser(authentication)).thenReturn(null);
        GestionTournament approved = tournament(9L, "Cup", ApprovalStatus.APPROVED);
        approved.setParticipantUserIds(new LinkedHashSet<>(List.of(requester.getId())));
        when(repository.findAll()).thenReturn(List.of(approved));

        var responses = service.getApproved(authentication);

        assertEquals(1, responses.size());
        assertFalse(responses.get(0).participating());
    }

    @Test
    void getRequests_requiresAdminAndSortsNewestFirst() {
        GestionTournament older = tournament(10L, "Older Cup", ApprovalStatus.PENDING);
        older.setCreatedAt(LocalDateTime.now().minusDays(2));
        GestionTournament newer = tournament(11L, "Newer Cup", ApprovalStatus.PENDING);
        newer.setCreatedAt(LocalDateTime.now().minusDays(1));

        when(support.requireAuthenticatedUser(authentication)).thenReturn(admin);
        when(repository.findAll()).thenReturn(List.of(older, newer));

        var responses = service.getRequests(authentication);

        assertEquals(List.of(11L, 10L), responses.stream().map(response -> response.id()).toList());
        verify(support).requireAdmin(admin);
    }

    @Test
    void submit_validatesDatesAndCreatesPendingRequest() {
        when(support.requireAuthenticatedUser(authentication)).thenReturn(requester);

        var response = service.submit(new TournamentRequest(" Summer Cup ", "2026-06-01", "2026-06-03", " Arena ", 8), authentication);

        assertEquals(ApprovalStatus.PENDING, response.approvalStatus());
        ArgumentCaptor<GestionTournament> captor = ArgumentCaptor.forClass(GestionTournament.class);
        verify(repository).save(captor.capture());
        assertEquals("Summer Cup", captor.getValue().getName());
        verify(support).validateDateRange("2026-06-01", "2026-06-03");
    }

    @Test
    void submit_propagatesDateRangeErrors() {
        when(support.requireAuthenticatedUser(authentication)).thenReturn(requester);
        doThrow(new IllegalArgumentException("La date de fin doit etre posterieure ou egale a la date de debut"))
                .when(support).validateDateRange("2026-06-03", "2026-06-01");

        assertThrows(IllegalArgumentException.class,
                () -> service.submit(new TournamentRequest("Summer Cup", "2026-06-03", "2026-06-01", "Arena", 8), authentication));
    }

    @Test
    void update_delete_approve_andReject_requireAdmin() {
        GestionTournament existing = tournament(6L, "Old Cup", ApprovalStatus.PENDING);
        when(support.requireAuthenticatedUser(authentication)).thenReturn(admin);
        when(repository.findById(6L)).thenReturn(Optional.of(existing));

        assertEquals("Updated", service.update(6L, new TournamentRequest("Updated", "2026-05-10", "2026-05-12", "New Arena", 16), authentication).name());
        assertEquals(ApprovalStatus.APPROVED, service.approve(6L, new DecisionRequest("ok"), authentication).approvalStatus());
        assertEquals(ApprovalStatus.REJECTED, service.reject(6L, new DecisionRequest("no"), authentication).approvalStatus());
        service.delete(6L, authentication);
        verify(repository).delete(existing);
    }

    @Test
    void approve_andReject_allowNullDecisionPayload() {
        GestionTournament existing = tournament(12L, "Pending Cup", ApprovalStatus.PENDING);
        when(support.requireAuthenticatedUser(authentication)).thenReturn(admin);
        when(repository.findById(12L)).thenReturn(Optional.of(existing));

        assertEquals(ApprovalStatus.APPROVED, service.approve(12L, null, authentication).approvalStatus());
        assertEquals(ApprovalStatus.REJECTED, service.reject(12L, null, authentication).approvalStatus());
        assertNull(existing.getApprovalNote());
    }

    @Test
    void participate_andCancelParticipation_workForApprovedTournament() {
        when(support.requireAuthenticatedUser(authentication)).thenReturn(requester);
        GestionTournament tournament = tournament(3L, "Cup", ApprovalStatus.APPROVED);
        when(repository.findById(3L)).thenReturn(Optional.of(tournament));

        assertTrue(service.participate(3L, authentication).participating());
        assertFalse(service.cancelParticipation(3L, authentication).participating());
    }

    @Test
    void participate_rejectsPendingTournament_andMissingTournamentThrows() {
        when(support.requireAuthenticatedUser(authentication)).thenReturn(requester, admin);
        GestionTournament tournament = tournament(4L, "Pending", ApprovalStatus.PENDING);
        when(repository.findById(4L)).thenReturn(Optional.of(tournament));

        assertThrows(IllegalArgumentException.class, () -> service.participate(4L, authentication));
        when(repository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.delete(5L, authentication));
    }

    private GestionTournament tournament(Long id, String name, ApprovalStatus status) {
        GestionTournament tournament = new GestionTournament();
        tournament.setId(id);
        tournament.setName(name);
        tournament.setStartDate("2026-05-01");
        tournament.setEndDate("2026-05-03");
        tournament.setLocation("Arena");
        tournament.setMaxTeams(8);
        tournament.setApprovalStatus(status);
        tournament.setParticipantUserIds(new LinkedHashSet<>());
        return tournament;
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
