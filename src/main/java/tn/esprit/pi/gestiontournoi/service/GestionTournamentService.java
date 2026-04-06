package tn.esprit.pi.gestiontournoi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.DecisionRequest;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.TournamentRequest;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.TournamentResponse;
import tn.esprit.pi.gestiontournoi.entity.ApprovalStatus;
import tn.esprit.pi.gestiontournoi.entity.GestionTournament;
import tn.esprit.pi.gestiontournoi.repository.GestionTournamentRepository;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GestionTournamentService {

    private final GestionTournamentRepository repository;
    private final GestionModuleSupport support;

    public List<TournamentResponse> getApproved(Authentication authentication) {
        User currentUser = support.getCurrentUser(authentication);
        return repository.findAll().stream()
                .filter(tournament -> support.getEffectiveStatus(tournament) == ApprovalStatus.APPROVED)
                .sorted(Comparator
                        .comparing(GestionTournament::getStartDate, Comparator.nullsLast(String::compareTo))
                        .thenComparing(GestionTournament::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(tournament -> toResponse(tournament, currentUser))
                .toList();
    }

    public List<TournamentResponse> getRequests(Authentication authentication) {
        User admin = support.requireAuthenticatedUser(authentication);
        support.requireAdmin(admin);
        return repository.findAll().stream()
                .sorted(Comparator
                        .comparing(GestionTournament::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(GestionTournament::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(tournament -> toResponse(tournament, admin))
                .toList();
    }

    public TournamentResponse submit(TournamentRequest request, Authentication authentication) {
        User requester = support.requireAuthenticatedUser(authentication);

        GestionTournament tournament = new GestionTournament();
        applyRequest(tournament, request);
        support.markPendingRequest(tournament, requester);

        return toResponse(repository.save(tournament), requester);
    }

    public TournamentResponse update(Long id, TournamentRequest request, Authentication authentication) {
        User admin = support.requireAuthenticatedUser(authentication);
        support.requireAdmin(admin);

        GestionTournament tournament = findById(id);
        applyRequest(tournament, request);
        return toResponse(repository.save(tournament), admin);
    }

    public void delete(Long id, Authentication authentication) {
        User admin = support.requireAuthenticatedUser(authentication);
        support.requireAdmin(admin);

        GestionTournament tournament = findById(id);
        repository.delete(tournament);
    }

    public TournamentResponse approve(Long id, DecisionRequest request, Authentication authentication) {
        User admin = support.requireAuthenticatedUser(authentication);
        support.requireAdmin(admin);

        GestionTournament tournament = findById(id);
        support.markDecision(tournament, ApprovalStatus.APPROVED, admin, request == null ? null : request.note());
        return toResponse(repository.save(tournament), admin);
    }

    public TournamentResponse reject(Long id, DecisionRequest request, Authentication authentication) {
        User admin = support.requireAuthenticatedUser(authentication);
        support.requireAdmin(admin);

        GestionTournament tournament = findById(id);
        support.markDecision(tournament, ApprovalStatus.REJECTED, admin, request == null ? null : request.note());
        return toResponse(repository.save(tournament), admin);
    }

    public TournamentResponse participate(Long id, Authentication authentication) {
        User user = support.requireAuthenticatedUser(authentication);

        GestionTournament tournament = findById(id);
        if (support.getEffectiveStatus(tournament) != ApprovalStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved tournaments can accept participants");
        }

        tournament.getParticipantUserIds().add(user.getId());
        return toResponse(repository.save(tournament), user);
    }

    public TournamentResponse cancelParticipation(Long id, Authentication authentication) {
        User user = support.requireAuthenticatedUser(authentication);

        GestionTournament tournament = findById(id);
        tournament.getParticipantUserIds().remove(user.getId());
        return toResponse(repository.save(tournament), user);
    }

    private GestionTournament findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
    }

    private void applyRequest(GestionTournament tournament, TournamentRequest request) {
        support.validateDateRange(request.startDate(), request.endDate());
        tournament.setName(support.requireText(request.name(), "Le nom du tournoi", 3, 100));
        tournament.setStartDate(support.requireIsoDate(request.startDate(), "La date de debut"));
        tournament.setEndDate(support.requireIsoDate(request.endDate(), "La date de fin"));
        tournament.setLocation(support.requireText(request.location(), "Le lieu du tournoi", 3, 120));
        tournament.setMaxTeams(support.requireInteger(request.maxTeams(), "Le nombre maximum d equipes", 2, 64));
    }

    private TournamentResponse toResponse(GestionTournament tournament, User currentUser) {
        boolean participating = currentUser != null && tournament.getParticipantUserIds().contains(currentUser.getId());
        return new TournamentResponse(
                tournament.getId(),
                tournament.getName(),
                tournament.getStartDate(),
                tournament.getEndDate(),
                tournament.getLocation(),
                tournament.getMaxTeams(),
                support.getEffectiveStatus(tournament),
                support.toUserSummary(tournament.getRequesterUserId(), tournament.getRequesterName(), tournament.getRequesterEmail()),
                support.toUserSummary(tournament.getApprovedByUserId(), tournament.getApprovedByName(), tournament.getApprovedByEmail()),
                tournament.getApprovalNote(),
                tournament.getParticipantUserIds().size(),
                participating
        );
    }
}
