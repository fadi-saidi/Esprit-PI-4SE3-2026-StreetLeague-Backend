package tn.esprit.pi.gestiontournoi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.CoachProfile;
import tn.esprit.pi.domain.Role;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.CoachSummary;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.DecisionRequest;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.PlayerSummary;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.TrainingLookupResponse;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.TrainingRequest;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.TrainingResponse;
import tn.esprit.pi.gestiontournoi.entity.ApprovalStatus;
import tn.esprit.pi.gestiontournoi.entity.GestionTraining;
import tn.esprit.pi.gestiontournoi.repository.GestionTrainingRepository;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GestionTrainingService {

    private final GestionTrainingRepository repository;
    private final GestionModuleSupport support;

    public List<TrainingResponse> getApproved(Authentication authentication) {
        User currentUser = support.getCurrentUser(authentication);
        return repository.findAll().stream()
                .filter(training -> support.getEffectiveStatus(training) == ApprovalStatus.APPROVED)
                .sorted(Comparator
                        .comparing(GestionTraining::getDate, Comparator.nullsLast(String::compareTo))
                        .thenComparing(GestionTraining::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(training -> toResponse(training, currentUser))
                .toList();
    }

    public List<TrainingResponse> getRequests(Authentication authentication) {
        User admin = support.requireAuthenticatedUser(authentication);
        support.requireAdmin(admin);
        return repository.findAll().stream()
                .sorted(Comparator
                        .comparing(GestionTraining::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(GestionTraining::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(training -> toResponse(training, admin))
                .toList();
    }

    public TrainingLookupResponse getLookups(Authentication authentication) {
        User coachUser = support.requireAuthenticatedUser(authentication);
        CoachProfile coachProfile = support.requireCoachProfile(coachUser);

        return new TrainingLookupResponse(
                new CoachSummary(coachUser.getId(), coachUser.getUsername()),
                support.getPlayersForCoach(coachProfile)
        );
    }

    public TrainingResponse submit(TrainingRequest request, Authentication authentication) {
        User coachUser = support.requireAuthenticatedUser(authentication);
        CoachProfile coachProfile = support.requireCoachProfile(coachUser);

        GestionTraining training = new GestionTraining();
        applyRequest(training, request, coachProfile);
        support.markPendingRequest(training, coachUser);

        return toResponse(repository.save(training), coachUser);
    }

    public TrainingResponse update(Long id, TrainingRequest request, Authentication authentication) {
        User user = support.requireAuthenticatedUser(authentication);
        GestionTraining training = findById(id);

        boolean admin = user.getRole() == Role.ADMIN;
        boolean coachOwner = user.getRole() == Role.COACH && Objects.equals(training.getCoachUserId(), user.getId());
        if (!admin && !coachOwner) {
            throw new AccessDeniedException("Only the assigned coach or an admin can update this training");
        }

        if (!admin && support.getEffectiveStatus(training) == ApprovalStatus.APPROVED) {
            throw new AccessDeniedException("Approved trainings can only be updated by an admin");
        }

        CoachProfile coachProfile = admin
                ? support.requireCoachProfileByUserId(training.getCoachUserId())
                : support.requireCoachProfile(user);

        applyRequest(training, request, coachProfile);
        if (!admin) {
            support.markPendingRequest(training, user);
        }

        return toResponse(repository.save(training), user);
    }

    public void delete(Long id, Authentication authentication) {
        User user = support.requireAuthenticatedUser(authentication);
        GestionTraining training = findById(id);

        boolean admin = user.getRole() == Role.ADMIN;
        boolean coachOwner = user.getRole() == Role.COACH && Objects.equals(training.getCoachUserId(), user.getId());
        boolean nonApprovedCoachDelete = coachOwner && support.getEffectiveStatus(training) != ApprovalStatus.APPROVED;
        if (!admin && !nonApprovedCoachDelete) {
            throw new AccessDeniedException("Only an admin can delete this training");
        }

        repository.delete(training);
    }

    public TrainingResponse approve(Long id, DecisionRequest request, Authentication authentication) {
        User admin = support.requireAuthenticatedUser(authentication);
        support.requireAdmin(admin);

        GestionTraining training = findById(id);
        support.markDecision(training, ApprovalStatus.APPROVED, admin, request == null ? null : request.note());
        return toResponse(repository.save(training), admin);
    }

    public TrainingResponse reject(Long id, DecisionRequest request, Authentication authentication) {
        User admin = support.requireAuthenticatedUser(authentication);
        support.requireAdmin(admin);

        GestionTraining training = findById(id);
        support.markDecision(training, ApprovalStatus.REJECTED, admin, request == null ? null : request.note());
        return toResponse(repository.save(training), admin);
    }

    private GestionTraining findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Training not found"));
    }

    private void applyRequest(GestionTraining training, TrainingRequest request, CoachProfile coachProfile) {
        Set<Long> selectedIds = support.requireSelectedIds(request.selectedPlayerUserIds(), "La liste des joueurs selectionnes");
        Set<Long> allowedIds = support.getAllowedPlayerIdsForCoach(coachProfile);
        if (!allowedIds.containsAll(selectedIds)) {
            throw new IllegalArgumentException("Les joueurs selectionnes doivent appartenir a une equipe du coach");
        }

        User coachUser = coachProfile.getUser();
        training.setTitle(support.requireText(request.title(), "Le titre du training", 3, 100));
        training.setCoachUserId(coachUser.getId());
        training.setCoachName(coachUser.getUsername());
        training.setDate(support.requireIsoDate(request.date(), "La date du training"));
        training.setDuration(support.requireInteger(request.duration(), "La duree du training", 1, 480));
        training.setLocation(support.requireText(request.location(), "Le lieu du training", 3, 120));
        training.setSelectedPlayerUserIds(selectedIds);
    }

    private TrainingResponse toResponse(GestionTraining training, User currentUser) {
        List<PlayerSummary> selectedPlayers = support.requirePlayers(training.getSelectedPlayerUserIds());

        return new TrainingResponse(
                training.getId(),
                training.getTitle(),
                new CoachSummary(training.getCoachUserId(), training.getCoachName()),
                training.getDate(),
                training.getDuration(),
                training.getLocation(),
                selectedPlayers,
                support.getEffectiveStatus(training),
                support.toUserSummary(training.getRequesterUserId(), training.getRequesterName(), training.getRequesterEmail()),
                support.toUserSummary(training.getApprovedByUserId(), training.getApprovedByName(), training.getApprovedByEmail()),
                training.getApprovalNote()
        );
    }
}
