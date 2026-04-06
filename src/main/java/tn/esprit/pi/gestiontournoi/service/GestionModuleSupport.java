package tn.esprit.pi.gestiontournoi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.CoachProfile;
import tn.esprit.pi.domain.PlayerProfile;
import tn.esprit.pi.domain.RefereeProfile;
import tn.esprit.pi.domain.Role;
import tn.esprit.pi.domain.Team;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.CoachSummary;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.PlayerSummary;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.RefereeSummary;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.TeamSummary;
import tn.esprit.pi.gestiontournoi.dto.GestionDtos.UserSummary;
import tn.esprit.pi.gestiontournoi.entity.ApprovalStatus;
import tn.esprit.pi.gestiontournoi.entity.GestionApprovalEntity;
import tn.esprit.pi.repository.CoachProfileRepository;
import tn.esprit.pi.repository.PlayerProfileRepository;
import tn.esprit.pi.repository.RefereeProfileRepository;
import tn.esprit.pi.repository.TeamRepository;
import tn.esprit.pi.repository.UserRepository;

import java.util.Collection;
import java.util.Comparator;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GestionModuleSupport {
    private static final Pattern SCORE_PATTERN = Pattern.compile("^\\d{1,2}\\s*-\\s*\\d{1,2}$");

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final RefereeProfileRepository refereeProfileRepository;
    private final CoachProfileRepository coachProfileRepository;
    private final PlayerProfileRepository playerProfileRepository;

    public User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String email = authentication.getName();
        if (email == null || email.isBlank() || "anonymousUser".equalsIgnoreCase(email)) {
            return null;
        }

        return userRepository.findByEmail(email).orElse(null);
    }

    public User requireAuthenticatedUser(Authentication authentication) {
        User user = getCurrentUser(authentication);
        if (user == null) {
            throw new AccessDeniedException("Authentication required");
        }
        return user;
    }

    public void requireAdmin(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Admin approval required");
        }
    }

    public CoachProfile requireCoachProfile(User user) {
        if (user.getRole() != Role.COACH) {
            throw new AccessDeniedException("Only coaches can manage trainings");
        }

        return coachProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Coach profile not found"));
    }

    public CoachProfile requireCoachProfileByUserId(Long coachUserId) {
        return coachProfileRepository.findByUserId(coachUserId)
                .orElseThrow(() -> new IllegalArgumentException("Coach profile not found"));
    }

    public TeamSummary requireTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));
        return new TeamSummary(team.getId(), team.getName());
    }

    public RefereeSummary requireReferee(Long refereeUserId) {
        RefereeProfile refereeProfile = refereeProfileRepository.findByUserId(refereeUserId)
                .orElseThrow(() -> new IllegalArgumentException("Referee not found"));
        return toRefereeSummary(refereeProfile);
    }

    public List<PlayerSummary> requirePlayers(Collection<Long> playerUserIds) {
        List<Long> requestedIds = playerUserIds == null
                ? List.of()
                : playerUserIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (requestedIds.isEmpty()) {
            return List.of();
        }

        Map<Long, PlayerSummary> summaries = playerProfileRepository.findByUserIdIn(requestedIds).stream()
                .collect(Collectors.toMap(
                        profile -> profile.getUser().getId(),
                        this::toPlayerSummary,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        if (summaries.size() != requestedIds.size()) {
            throw new IllegalArgumentException("One or more selected players do not exist");
        }

        return requestedIds.stream()
                .map(summaries::get)
                .toList();
    }

    public List<PlayerSummary> getPlayersForCoach(CoachProfile coachProfile) {
        return playerProfileRepository.findDistinctByTeams_CoachProfile_Id(coachProfile.getId()).stream()
                .map(this::toPlayerSummary)
                .sorted(Comparator.comparing(PlayerSummary::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Set<Long> getAllowedPlayerIdsForCoach(CoachProfile coachProfile) {
        return playerProfileRepository.findDistinctByTeams_CoachProfile_Id(coachProfile.getId()).stream()
                .map(PlayerProfile::getUser)
                .filter(Objects::nonNull)
                .map(User::getId)
                .collect(Collectors.toSet());
    }

    public List<TeamSummary> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(team -> new TeamSummary(team.getId(), team.getName()))
                .sorted(Comparator.comparing(TeamSummary::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<RefereeSummary> getAllReferees() {
        return refereeProfileRepository.findAll().stream()
                .map(this::toRefereeSummary)
                .sorted(Comparator.comparing(RefereeSummary::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public UserSummary toUserSummary(Long userId, String name, String email) {
        if (userId == null && (name == null || name.isBlank()) && (email == null || email.isBlank())) {
            return null;
        }

        User user = userId == null ? null : userRepository.findById(userId).orElse(null);
        return new UserSummary(
                user != null ? user.getId() : userId,
                user != null ? user.getUsername() : name,
                user != null ? user.getEmail() : email,
                user != null && user.getRole() != null ? user.getRole().name() : null
        );
    }

    public String normalizeOptionalText(String value) {
        String normalized = value == null ? "" : value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public String normalizeOptionalTextWithLimit(String value, int maxLength, String fieldLabel) {
        String normalized = normalizeOptionalText(value);
        if (normalized != null && normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldLabel + " ne peut pas depasser " + maxLength + " caracteres");
        }
        return normalized;
    }

    public String requireText(String value, String fieldLabel, int minLength, int maxLength) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldLabel + " est obligatoire");
        }
        if (normalized.length() < minLength) {
            throw new IllegalArgumentException(fieldLabel + " doit contenir au moins " + minLength + " caracteres");
        }
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldLabel + " ne peut pas depasser " + maxLength + " caracteres");
        }
        return normalized;
    }

    public String requireIsoDate(String value, String fieldLabel) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldLabel + " est obligatoire");
        }
        if (!normalized.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            throw new IllegalArgumentException(fieldLabel + " doit respecter le format yyyy-MM-dd");
        }
        try {
            return LocalDate.parse(normalized).toString();
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(fieldLabel + " doit correspondre a une date valide au format yyyy-MM-dd");
        }
    }

    public Integer requireInteger(Integer value, String fieldLabel, int min, int max) {
        if (value == null) {
            throw new IllegalArgumentException(fieldLabel + " est obligatoire");
        }
        if (value < min || value > max) {
            throw new IllegalArgumentException(fieldLabel + " doit etre compris entre " + min + " et " + max);
        }
        return value;
    }

    public String normalizeScore(String value) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            return null;
        }
        if (!SCORE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Le score doit respecter le format 2 - 1");
        }
        return normalized.replaceAll("\\s*-\\s*", " - ");
    }

    public LinkedHashSet<Long> requireSelectedIds(Collection<Long> values, String fieldLabel) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException(fieldLabel + " est obligatoire");
        }

        LinkedHashSet<Long> normalized = new LinkedHashSet<>();
        for (Long value : values) {
            if (value == null) {
                throw new IllegalArgumentException(fieldLabel + " contient un identifiant invalide");
            }
            normalized.add(value);
        }

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldLabel + " est obligatoire");
        }

        return normalized;
    }

    public void validateDateRange(String startDate, String endDate) {
        LocalDate parsedStart = LocalDate.parse(requireIsoDate(startDate, "La date de debut"));
        LocalDate parsedEnd = LocalDate.parse(requireIsoDate(endDate, "La date de fin"));
        if (parsedStart.isAfter(parsedEnd)) {
            throw new IllegalArgumentException("La date de fin doit etre posterieure ou egale a la date de debut");
        }
    }

    public void validateDistinctTeams(Long homeTeamId, Long awayTeamId) {
        if (Objects.equals(homeTeamId, awayTeamId)) {
            throw new IllegalArgumentException("A match must contain two different teams");
        }
    }

    public ApprovalStatus getEffectiveStatus(GestionApprovalEntity entity) {
        return entity.getApprovalStatus() == null ? ApprovalStatus.APPROVED : entity.getApprovalStatus();
    }

    public void markPendingRequest(GestionApprovalEntity entity, User requester) {
        entity.setApprovalStatus(ApprovalStatus.PENDING);
        entity.setRequesterUserId(requester.getId());
        entity.setRequesterName(requester.getUsername());
        entity.setRequesterEmail(requester.getEmail());
        entity.setApprovedByUserId(null);
        entity.setApprovedByName(null);
        entity.setApprovedByEmail(null);
        entity.setApprovalNote(null);
    }

    public void markDecision(GestionApprovalEntity entity, ApprovalStatus status, User admin, String note) {
        entity.setApprovalStatus(status);
        entity.setApprovedByUserId(admin.getId());
        entity.setApprovedByName(admin.getUsername());
        entity.setApprovedByEmail(admin.getEmail());
        entity.setApprovalNote(normalizeOptionalText(note));
    }

    private PlayerSummary toPlayerSummary(PlayerProfile profile) {
        User user = profile.getUser();
        return new PlayerSummary(user.getId(), user.getUsername());
    }

    private RefereeSummary toRefereeSummary(RefereeProfile profile) {
        User user = profile.getUser();
        return new RefereeSummary(user.getId(), user.getUsername());
    }
}
