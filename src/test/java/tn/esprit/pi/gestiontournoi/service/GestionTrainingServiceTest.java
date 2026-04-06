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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import tn.esprit.pi.domain.CoachProfile;
import tn.esprit.pi.domain.Role;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.DecisionRequest;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.PlayerSummary;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.TrainingRequest;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.UserSummary;
import tn.esprit.pi.gestiontournoi.entity.ApprovalStatus;
import tn.esprit.pi.gestiontournoi.entity.GestionApprovalEntity;
import tn.esprit.pi.gestiontournoi.entity.GestionTraining;
import tn.esprit.pi.gestiontournoi.repository.GestionTrainingRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GestionTrainingServiceTest {

    @Mock
    private GestionTrainingRepository repository;
    @Mock
    private GestionModuleSupport support;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private GestionTrainingService service;

    private User admin;
    private User coachUser;
    private User otherCoachUser;
    private CoachProfile coachProfile;

    @BeforeEach
    void setUp() {
        admin = user(1L, "Admin", "admin@test.com", Role.ADMIN);
        coachUser = user(2L, "Coach", "coach@test.com", Role.COACH);
        otherCoachUser = user(3L, "Other", "other@test.com", Role.COACH);
        coachProfile = new CoachProfile();
        coachProfile.setId(2L);
        coachProfile.setUser(coachUser);

        when(support.getEffectiveStatus(any(GestionApprovalEntity.class))).thenAnswer(invocation -> {
            GestionApprovalEntity entity = invocation.getArgument(0);
            return entity.getApprovalStatus() == null ? ApprovalStatus.APPROVED : entity.getApprovalStatus();
        });
        when(support.toUserSummary(any(), any(), any())).thenAnswer(invocation ->
                new UserSummary(invocation.getArgument(0), invocation.getArgument(1), invocation.getArgument(2), null));
        when(support.requireText(anyString(), anyString(), anyInt(), anyInt())).thenAnswer(invocation ->
                invocation.getArgument(0, String.class).trim());
        when(support.requireIsoDate(anyString(), anyString())).thenAnswer(invocation ->
                invocation.getArgument(0, String.class).trim());
        when(support.requireInteger(any(), anyString(), anyInt(), anyInt())).thenAnswer(invocation ->
                invocation.getArgument(0, Integer.class));
        when(support.requireSelectedIds(any(), anyString())).thenAnswer(invocation -> {
            java.util.Collection<Long> ids = invocation.getArgument(0);
            return ids == null ? new LinkedHashSet<>() : new LinkedHashSet<>(ids);
        });
        doAnswer(invocation -> {
            GestionTraining training = invocation.getArgument(0);
            User user = invocation.getArgument(1);
            training.setApprovalStatus(ApprovalStatus.PENDING);
            training.setRequesterUserId(user.getId());
            training.setRequesterName(user.getUsername());
            training.setRequesterEmail(user.getEmail());
            return null;
        }).when(support).markPendingRequest(any(GestionTraining.class), any(User.class));
        doAnswer(invocation -> {
            GestionTraining training = invocation.getArgument(0);
            ApprovalStatus status = invocation.getArgument(1);
            User user = invocation.getArgument(2);
            String note = invocation.getArgument(3);
            training.setApprovalStatus(status);
            training.setApprovedByUserId(user.getId());
            training.setApprovedByName(user.getUsername());
            training.setApprovedByEmail(user.getEmail());
            training.setApprovalNote(note);
            return null;
        }).when(support).markDecision(any(GestionTraining.class), any(ApprovalStatus.class), any(User.class), any());
        when(repository.save(any(GestionTraining.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(support.requirePlayers(any())).thenReturn(List.of(new PlayerSummary(9L, "Player One")));
    }

    @Test
    void getApproved_andGetRequests_returnMappedResponses() {
        when(support.getCurrentUser(authentication)).thenReturn(coachUser);
        when(support.requireAuthenticatedUser(authentication)).thenReturn(admin);
        when(repository.findAll()).thenReturn(List.of(training(1L, ApprovalStatus.APPROVED), training(2L, ApprovalStatus.PENDING)));

        assertEquals(1, service.getApproved(authentication).size());
        assertEquals(2, service.getRequests(authentication).size());
    }

    @Test
    void getLookups_andSubmit_areCoachDriven() {
        when(support.requireAuthenticatedUser(authentication)).thenReturn(coachUser);
        when(support.requireCoachProfile(coachUser)).thenReturn(coachProfile);
        when(support.getPlayersForCoach(coachProfile)).thenReturn(List.of(new PlayerSummary(9L, "Player One")));
        when(support.getAllowedPlayerIdsForCoach(coachProfile)).thenReturn(Set.of(9L));

        var lookups = service.getLookups(authentication);
        assertEquals("Coach", lookups.currentCoach().name());
        assertEquals(1, lookups.availablePlayers().size());

        var submitted = service.submit(new TrainingRequest("Evening", "2026-05-09", 75, "Center", List.of(9L)), authentication);
        assertEquals(ApprovalStatus.PENDING, submitted.approvalStatus());

        ArgumentCaptor<GestionTraining> captor = ArgumentCaptor.forClass(GestionTraining.class);
        verify(repository).save(captor.capture());
        assertEquals(coachUser.getId(), captor.getValue().getCoachUserId());
        assertEquals(new LinkedHashSet<>(List.of(9L)), captor.getValue().getSelectedPlayerUserIds());
    }

    @Test
    void update_allowsAdminAndCoachOwnerButRejectsForbiddenCases() {
        GestionTraining pending = training(5L, ApprovalStatus.PENDING);
        when(repository.findById(5L)).thenReturn(Optional.of(pending));
        when(support.requireAuthenticatedUser(authentication)).thenReturn(admin, coachUser, otherCoachUser, coachUser);
        when(support.requireCoachProfileByUserId(2L)).thenReturn(coachProfile);
        when(support.requireCoachProfile(coachUser)).thenReturn(coachProfile);
        when(support.getAllowedPlayerIdsForCoach(coachProfile)).thenReturn(Set.of(9L));

        assertEquals("Updated", service.update(5L, new TrainingRequest("Updated", "2026-05-10", 60, "Hall", List.of(9L)), authentication).title());

        pending.setApprovalStatus(ApprovalStatus.PENDING);
        assertEquals(ApprovalStatus.PENDING, service.update(5L, new TrainingRequest("Coach Update", "2026-05-11", 60, "Hall", List.of(9L)), authentication).approvalStatus());

        assertThrows(AccessDeniedException.class,
                () -> service.update(5L, new TrainingRequest("Blocked", "2026-05-11", 60, "Hall", List.of(9L)), authentication));

        pending.setApprovalStatus(ApprovalStatus.APPROVED);
        assertThrows(AccessDeniedException.class,
                () -> service.update(5L, new TrainingRequest("Blocked", "2026-05-11", 60, "Hall", List.of(9L)), authentication));
    }

    @Test
    void delete_approve_andReject_followPermissions() {
        GestionTraining pending = training(11L, ApprovalStatus.PENDING);
        GestionTraining approved = training(12L, ApprovalStatus.APPROVED);
        when(repository.findById(11L)).thenReturn(Optional.of(pending));
        when(repository.findById(12L)).thenReturn(Optional.of(approved));
        when(support.requireAuthenticatedUser(authentication)).thenReturn(coachUser, coachUser, admin, admin, admin);

        service.delete(11L, authentication);
        verify(repository).delete(pending);

        assertThrows(AccessDeniedException.class, () -> service.delete(12L, authentication));

        assertEquals(ApprovalStatus.APPROVED, service.approve(11L, new DecisionRequest("ok"), authentication).approvalStatus());
        assertEquals(ApprovalStatus.REJECTED, service.reject(11L, new DecisionRequest("no"), authentication).approvalStatus());
        service.delete(12L, authentication);
        verify(repository).delete(approved);
    }

    @Test
    void submit_rejectsPlayersOutsideCoachTeams_andMissingTrainingThrows() {
        when(support.requireAuthenticatedUser(authentication)).thenReturn(coachUser, admin);
        when(support.requireCoachProfile(coachUser)).thenReturn(coachProfile);
        when(support.getAllowedPlayerIdsForCoach(coachProfile)).thenReturn(Set.of(9L));

        assertThrows(IllegalArgumentException.class,
                () -> service.submit(new TrainingRequest("Evening", "2026-05-09", 75, "Center", List.of(10L)), authentication));

        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.delete(99L, authentication));
    }

    @Test
    void submit_rejectsNullSelectedPlayerIds() {
        when(support.requireAuthenticatedUser(authentication)).thenReturn(coachUser);
        when(support.requireCoachProfile(coachUser)).thenReturn(coachProfile);
        doThrow(new IllegalArgumentException("La liste des joueurs selectionnes contient un identifiant invalide"))
                .when(support).requireSelectedIds(any(), anyString());

        assertThrows(IllegalArgumentException.class,
                () -> service.submit(new TrainingRequest("Evening", "2026-05-09", 75, "Center", java.util.Arrays.asList(9L, null)), authentication));
    }

    private GestionTraining training(Long id, ApprovalStatus status) {
        GestionTraining training = new GestionTraining();
        training.setId(id);
        training.setTitle("Session");
        training.setCoachUserId(2L);
        training.setCoachName("Coach");
        training.setDate("2026-05-01");
        training.setDuration(60);
        training.setLocation("Hall");
        training.setSelectedPlayerUserIds(new LinkedHashSet<>(List.of(9L)));
        training.setApprovalStatus(status);
        return training;
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
