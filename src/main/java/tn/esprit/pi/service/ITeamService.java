package tn.esprit.pi.service;

import tn.esprit.pi.dto.JoinRequestDTO;
import tn.esprit.pi.dto.PlayerSummaryDTO;
import tn.esprit.pi.dto.TeamDTO;

import java.util.List;

public interface ITeamService {

    // ── CRUD ──────────────────────────────────────────────────────────────────
    TeamDTO createTeam(TeamDTO dto);
    TeamDTO updateTeam(Long id, TeamDTO dto);
    TeamDTO getTeamById(Long id);
    List<TeamDTO> getAllTeams();
    void deleteTeam(Long id);
    List<TeamDTO> getTeamsByUserId(Long userId);

    // ── Join requests ─────────────────────────────────────────────────────────
    JoinRequestDTO requestJoin(Long teamId, Long playerId);
    JoinRequestDTO invitePlayer(Long teamId, Long playerId);
    List<JoinRequestDTO> getPendingRequestsForTeam(Long teamId);
    List<JoinRequestDTO> getInvitationsForPlayer(Long userId);
    void acceptRequest(Long requestId);
    void refuseRequest(Long requestId);

    // ── Captain management ────────────────────────────────────────────────────
    TeamDTO transferCaptain(Long teamId, Long newCaptainId);
    void removePlayer(Long teamId, Long userId);

    // ── Players list ──────────────────────────────────────────────────────────
    List<PlayerSummaryDTO> getAllPlayers();
}
