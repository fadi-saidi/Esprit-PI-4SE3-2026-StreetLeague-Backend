package tn.esprit.pi.gestiontournoi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.DecisionRequest;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.MatchLookupResponse;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.MatchRequest;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.MatchResponse;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.RefereeSummary;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.TeamSummary;
import tn.esprit.pi.gestiontournoi.entity.ApprovalStatus;
import tn.esprit.pi.gestiontournoi.entity.GestionMatch;
import tn.esprit.pi.gestiontournoi.repository.GestionMatchRepository;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GestionMatchService {

    private final GestionMatchRepository repository;
    private final GestionModuleSupport support;

    public List<MatchResponse> getApproved(Authentication authentication) {
        User currentUser = support.getCurrentUser(authentication);
        return repository.findAll().stream()
                .filter(match -> support.getEffectiveStatus(match) == ApprovalStatus.APPROVED)
                .sorted(Comparator
                        .comparing(GestionMatch::getDate, Comparator.nullsLast(String::compareTo))
                        .thenComparing(GestionMatch::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(match -> toResponse(match, currentUser))
                .toList();
    }

    public List<MatchResponse> getRequests(Authentication authentication) {
        User admin = support.requireAuthenticatedUser(authentication);
        support.requireAdmin(admin);
        return repository.findAll().stream()
                .sorted(Comparator
                        .comparing(GestionMatch::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(GestionMatch::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(match -> toResponse(match, admin))
                .toList();
    }

    public MatchLookupResponse getLookups(Authentication authentication) {
        support.requireAuthenticatedUser(authentication);
        return new MatchLookupResponse(support.getAllTeams(), support.getAllReferees());
    }

    public MatchResponse submit(MatchRequest request, Authentication authentication) {
        User requester = support.requireAuthenticatedUser(authentication);

        GestionMatch match = new GestionMatch();
        applyRequest(match, request);
        support.markPendingRequest(match, requester);

        return toResponse(repository.save(match), requester);
    }

    public MatchResponse update(Long id, MatchRequest request, Authentication authentication) {
        User admin = support.requireAuthenticatedUser(authentication);
        support.requireAdmin(admin);

        GestionMatch match = findById(id);
        applyRequest(match, request);
        return toResponse(repository.save(match), admin);
    }

    public void delete(Long id, Authentication authentication) {
        User admin = support.requireAuthenticatedUser(authentication);
        support.requireAdmin(admin);

        GestionMatch match = findById(id);
        repository.delete(match);
    }

    public MatchResponse approve(Long id, DecisionRequest request, Authentication authentication) {
        User admin = support.requireAuthenticatedUser(authentication);
        support.requireAdmin(admin);

        GestionMatch match = findById(id);
        support.markDecision(match, ApprovalStatus.APPROVED, admin, request == null ? null : request.note());
        return toResponse(repository.save(match), admin);
    }

    public MatchResponse reject(Long id, DecisionRequest request, Authentication authentication) {
        User admin = support.requireAuthenticatedUser(authentication);
        support.requireAdmin(admin);

        GestionMatch match = findById(id);
        support.markDecision(match, ApprovalStatus.REJECTED, admin, request == null ? null : request.note());
        return toResponse(repository.save(match), admin);
    }

    private GestionMatch findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
    }

    private void applyRequest(GestionMatch match, MatchRequest request) {
        support.validateDistinctTeams(request.homeTeamId(), request.awayTeamId());
        TeamSummary homeTeam = support.requireTeam(request.homeTeamId());
        TeamSummary awayTeam = support.requireTeam(request.awayTeamId());
        RefereeSummary referee = support.requireReferee(request.refereeUserId());

        match.setHomeTeamId(homeTeam.id());
        match.setHomeTeamName(homeTeam.name());
        match.setAwayTeamId(awayTeam.id());
        match.setAwayTeamName(awayTeam.name());
        match.setRefereeUserId(referee.userId());
        match.setRefereeName(referee.name());
        match.setDate(support.requireIsoDate(request.date(), "La date du match"));
        match.setScore(support.normalizeScore(request.score()));
        match.setLocation(support.requireText(request.location(), "Le lieu du match", 3, 120));
    }

    private MatchResponse toResponse(GestionMatch match, User currentUser) {
        TeamSummary homeTeam = new TeamSummary(match.getHomeTeamId(), match.getHomeTeamName());
        TeamSummary awayTeam = new TeamSummary(match.getAwayTeamId(), match.getAwayTeamName());
        RefereeSummary referee = new RefereeSummary(match.getRefereeUserId(), match.getRefereeName());

        return new MatchResponse(
                match.getId(),
                homeTeam,
                awayTeam,
                referee,
                match.getDate(),
                match.getScore(),
                match.getLocation(),
                support.getEffectiveStatus(match),
                support.toUserSummary(match.getRequesterUserId(), match.getRequesterName(), match.getRequesterEmail()),
                support.toUserSummary(match.getApprovedByUserId(), match.getApprovedByName(), match.getApprovedByEmail()),
                match.getApprovalNote()
        );
    }
}
