package tn.esprit.pi.gestiontournoi.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import tn.esprit.pi.gestiontournoi.entity.ApprovalStatus;

import java.util.List;

public class GestionDtos {

    public record UserSummary(Long id, String name, String email, String role) {}

    public record TeamSummary(Long id, String name) {}

    public record RefereeSummary(Long userId, String name) {}

    public record CoachSummary(Long userId, String name) {}

    public record PlayerSummary(Long userId, String name) {}

    public record DecisionRequest(String note) {}

    public record EventRequest(
            @NotBlank(message = "Le nom de l evenement est obligatoire")
            @Size(min = 3, max = 100, message = "Le nom de l evenement doit contenir entre 3 et 100 caracteres")
            String name,
            @Size(max = 500, message = "La description de l evenement ne peut pas depasser 500 caracteres")
            String description,
            @NotBlank(message = "La date de l evenement est obligatoire")
            @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "La date de l evenement doit respecter le format yyyy-MM-dd")
            String date,
            @NotBlank(message = "Le lieu de l evenement est obligatoire")
            @Size(min = 3, max = 120, message = "Le lieu de l evenement doit contenir entre 3 et 120 caracteres")
            String location
    ) {}

    public record EventResponse(
            Long id,
            String name,
            String description,
            String date,
            String location,
            ApprovalStatus approvalStatus,
            UserSummary requestedBy,
            UserSummary approvedBy,
            String approvalNote,
            int participantCount,
            boolean participating
    ) {}

    public record TournamentRequest(
            @NotBlank(message = "Le nom du tournoi est obligatoire")
            @Size(min = 3, max = 100, message = "Le nom du tournoi doit contenir entre 3 et 100 caracteres")
            String name,
            @NotBlank(message = "La date de debut est obligatoire")
            @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "La date de debut doit respecter le format yyyy-MM-dd")
            String startDate,
            @NotBlank(message = "La date de fin est obligatoire")
            @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "La date de fin doit respecter le format yyyy-MM-dd")
            String endDate,
            @NotBlank(message = "Le lieu du tournoi est obligatoire")
            @Size(min = 3, max = 120, message = "Le lieu du tournoi doit contenir entre 3 et 120 caracteres")
            String location,
            @NotNull(message = "Le nombre maximum d equipes est obligatoire")
            @Min(value = 2, message = "Le tournoi doit accepter au moins 2 equipes")
            @Max(value = 64, message = "Le tournoi ne peut pas depasser 64 equipes")
            Integer maxTeams
    ) {}

    public record TournamentResponse(
            Long id,
            String name,
            String startDate,
            String endDate,
            String location,
            Integer maxTeams,
            ApprovalStatus approvalStatus,
            UserSummary requestedBy,
            UserSummary approvedBy,
            String approvalNote,
            int participantCount,
            boolean participating
    ) {}

    public record MatchRequest(
            @NotNull(message = "L equipe domicile est obligatoire")
            Long homeTeamId,
            @NotNull(message = "L equipe visiteur est obligatoire")
            Long awayTeamId,
            @NotNull(message = "L arbitre est obligatoire")
            Long refereeUserId,
            @NotBlank(message = "La date du match est obligatoire")
            @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "La date du match doit respecter le format yyyy-MM-dd")
            String date,
            @Pattern(
                    regexp = "^\\s*$|^\\d{1,2}\\s*-\\s*\\d{1,2}$",
                    message = "Le score doit respecter le format 2 - 1"
            )
            String score,
            @NotBlank(message = "Le lieu du match est obligatoire")
            @Size(min = 3, max = 120, message = "Le lieu du match doit contenir entre 3 et 120 caracteres")
            String location
    ) {}

    public record MatchResponse(
            Long id,
            TeamSummary homeTeam,
            TeamSummary awayTeam,
            RefereeSummary referee,
            String date,
            String score,
            String location,
            ApprovalStatus approvalStatus,
            UserSummary requestedBy,
            UserSummary approvedBy,
            String approvalNote
    ) {}

    public record TrainingRequest(
            @NotBlank(message = "Le titre du training est obligatoire")
            @Size(min = 3, max = 100, message = "Le titre du training doit contenir entre 3 et 100 caracteres")
            String title,
            @NotBlank(message = "La date du training est obligatoire")
            @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "La date du training doit respecter le format yyyy-MM-dd")
            String date,
            @NotNull(message = "La duree du training est obligatoire")
            @Min(value = 1, message = "La duree du training doit etre d au moins 1 minute")
            @Max(value = 480, message = "La duree du training ne peut pas depasser 480 minutes")
            Integer duration,
            @NotBlank(message = "Le lieu du training est obligatoire")
            @Size(min = 3, max = 120, message = "Le lieu du training doit contenir entre 3 et 120 caracteres")
            String location,
            @NotEmpty(message = "Selectionnez au moins un joueur")
            List<@NotNull(message = "Chaque joueur selectionne doit etre valide") Long> selectedPlayerUserIds
    ) {}

    public record TrainingResponse(
            Long id,
            String title,
            CoachSummary coach,
            String date,
            Integer duration,
            String location,
            List<PlayerSummary> selectedPlayers,
            ApprovalStatus approvalStatus,
            UserSummary requestedBy,
            UserSummary approvedBy,
            String approvalNote
    ) {}

    public record MatchLookupResponse(
            List<TeamSummary> teams,
            List<RefereeSummary> referees
    ) {}

    public record TrainingLookupResponse(
            CoachSummary currentCoach,
            List<PlayerSummary> availablePlayers
    ) {}
}
