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
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.EventRequest;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.UserSummary;
import tn.esprit.pi.gestiontournoi.entity.ApprovalStatus;
import tn.esprit.pi.gestiontournoi.entity.GestionApprovalEntity;
import tn.esprit.pi.gestiontournoi.entity.GestionEvent;
import tn.esprit.pi.gestiontournoi.repository.GestionEventRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
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
class GestionEventServiceTest {

    @Mock
    private GestionEventRepository repository;
    @Mock
    private GestionModuleSupport support;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private GestionEventService service;

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
        when(support.normalizeOptionalText(any())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            if (value == null) {
                return null;
            }
            String trimmed = value.trim();
            return trimmed.isEmpty() ? null : trimmed;
        });
        when(support.normalizeOptionalTextWithLimit(nullable(String.class), anyInt(), anyString())).thenAnswer(invocation -> {
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
        when(support.toUserSummary(any(), any(), any())).thenAnswer(invocation ->
                new UserSummary(invocation.getArgument(0), invocation.getArgument(1), invocation.getArgument(2), null));
        doAnswer(invocation -> {
            GestionEvent event = invocation.getArgument(0);
            User user = invocation.getArgument(1);
            event.setApprovalStatus(ApprovalStatus.PENDING);
            event.setRequesterUserId(user.getId());
            event.setRequesterName(user.getUsername());
            event.setRequesterEmail(user.getEmail());
            event.setApprovedByUserId(null);
            event.setApprovedByName(null);
            event.setApprovedByEmail(null);
            event.setApprovalNote(null);
            return null;
        }).when(support).markPendingRequest(any(GestionEvent.class), any(User.class));
        doAnswer(invocation -> {
            GestionEvent event = invocation.getArgument(0);
            ApprovalStatus status = invocation.getArgument(1);
            User user = invocation.getArgument(2);
            String note = invocation.getArgument(3);
            event.setApprovalStatus(status);
            event.setApprovedByUserId(user.getId());
            event.setApprovedByName(user.getUsername());
            event.setApprovedByEmail(user.getEmail());
            event.setApprovalNote(note);
            return null;
        }).when(support).markDecision(any(GestionEvent.class), any(ApprovalStatus.class), any(User.class), any());
        when(repository.save(any(GestionEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void getApproved_filtersPendingEventsAndMarksParticipation() {
        when(support.getCurrentUser(authentication)).thenReturn(requester);

        GestionEvent approved = event(1L, "Approved", ApprovalStatus.APPROVED);
        approved.setParticipantUserIds(new LinkedHashSet<>(List.of(2L)));
        GestionEvent pending = event(2L, "Pending", ApprovalStatus.PENDING);
        when(repository.findAll()).thenReturn(List.of(pending, approved));

        var responses = service.getApproved(authentication);

        assertEquals(1, responses.size());
        assertEquals("Approved", responses.get(0).name());
        assertTrue(responses.get(0).participating());
    }

    @Test
    void getApproved_handlesAnonymousUserWithoutParticipation() {
        when(support.getCurrentUser(authentication)).thenReturn(null);

        GestionEvent approved = event(3L, "Approved", ApprovalStatus.APPROVED);
        approved.setParticipantUserIds(new LinkedHashSet<>(List.of(requester.getId())));
        when(repository.findAll()).thenReturn(List.of(approved));

        var responses = service.getApproved(authentication);

        assertEquals(1, responses.size());
        assertFalse(responses.get(0).participating());
    }

    @Test
    void getRequests_requiresAdminAndSortsNewestFirst() {
        GestionEvent older = event(10L, "Older", ApprovalStatus.PENDING);
        older.setCreatedAt(LocalDateTime.now().minusDays(2));
        GestionEvent newer = event(11L, "Newer", ApprovalStatus.PENDING);
        newer.setCreatedAt(LocalDateTime.now().minusDays(1));

        when(support.requireAuthenticatedUser(authentication)).thenReturn(admin);
        when(repository.findAll()).thenReturn(List.of(older, newer));

        var responses = service.getRequests(authentication);

        assertEquals(List.of(11L, 10L), responses.stream().map(response -> response.id()).toList());
        verify(support).requireAdmin(admin);
    }

    @Test
    void submit_createsPendingRequest() {
        when(support.requireAuthenticatedUser(authentication)).thenReturn(requester);

        var response = service.submit(new EventRequest(" Match Day ", " desc ", "2026-04-09", " Arena "), authentication);

        assertEquals(ApprovalStatus.PENDING, response.approvalStatus());
        assertEquals("Match Day", response.name());
        assertEquals("desc", response.description());

        ArgumentCaptor<GestionEvent> captor = ArgumentCaptor.forClass(GestionEvent.class);
        verify(repository).save(captor.capture());
        assertEquals("Arena", captor.getValue().getLocation());
        assertEquals(requester.getId(), captor.getValue().getRequesterUserId());
    }

    @Test
    void update_delete_approve_andReject_requireAdmin() {
        GestionEvent existing = event(7L, "Old", ApprovalStatus.PENDING);
        when(support.requireAuthenticatedUser(authentication)).thenReturn(admin);
        when(repository.findById(7L)).thenReturn(Optional.of(existing));

        var updated = service.update(7L, new EventRequest("New", "", "2026-04-12", "New Place"), authentication);
        assertEquals("New", updated.name());

        var approved = service.approve(7L, new DecisionRequest("Approved"), authentication);
        assertEquals(ApprovalStatus.APPROVED, approved.approvalStatus());

        var rejected = service.reject(7L, new DecisionRequest("Rejected"), authentication);
        assertEquals(ApprovalStatus.REJECTED, rejected.approvalStatus());

        service.delete(7L, authentication);
        verify(repository).delete(existing);
    }

    @Test
    void approve_andReject_allowNullDecisionPayload() {
        GestionEvent existing = event(12L, "Pending", ApprovalStatus.PENDING);
        when(support.requireAuthenticatedUser(authentication)).thenReturn(admin);
        when(repository.findById(12L)).thenReturn(Optional.of(existing));

        assertEquals(ApprovalStatus.APPROVED, service.approve(12L, null, authentication).approvalStatus());
        assertEquals(ApprovalStatus.REJECTED, service.reject(12L, null, authentication).approvalStatus());
        assertNull(existing.getApprovalNote());
    }

    @Test
    void participate_andCancelParticipation_updateMembership() {
        when(support.requireAuthenticatedUser(authentication)).thenReturn(requester);
        GestionEvent event = event(4L, "Open Event", ApprovalStatus.APPROVED);
        when(repository.findById(4L)).thenReturn(Optional.of(event));

        var joined = service.participate(4L, authentication);
        assertTrue(joined.participating());
        assertEquals(1, joined.participantCount());

        var cancelled = service.cancelParticipation(4L, authentication);
        assertFalse(cancelled.participating());
        assertEquals(0, cancelled.participantCount());
    }

    @Test
    void participate_rejectsPendingEvent_andMissingEventThrows() {
        when(support.requireAuthenticatedUser(authentication)).thenReturn(requester, admin);
        GestionEvent pending = event(8L, "Pending", ApprovalStatus.PENDING);
        when(repository.findById(8L)).thenReturn(Optional.of(pending));

        assertThrows(IllegalArgumentException.class, () -> service.participate(8L, authentication));
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.update(9L, new EventRequest("A", "", "2026-04-10", "B"), authentication));
    }

    private GestionEvent event(Long id, String name, ApprovalStatus status) {
        GestionEvent event = new GestionEvent();
        event.setId(id);
        event.setName(name);
        event.setDescription("desc");
        event.setDate("2026-04-10");
        event.setLocation("Arena");
        event.setApprovalStatus(status);
        event.setParticipantUserIds(new LinkedHashSet<>());
        return event;
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
