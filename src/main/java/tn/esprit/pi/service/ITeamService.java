package tn.esprit.pi.service;

import tn.esprit.pi.domain.JoinRequest;
import tn.esprit.pi.domain.Team;
import tn.esprit.pi.domain.User;

import java.util.List;

public interface ITeamService {
    Team createTeam(Team team);
    Team updateTeam(Long id, Team team);
    Team getTeamById(Long id);
    List<Team> getAllTeams();
    void deleteTeam(Long id);
    List<Team> getTeamsByUserId(Long userId);

    JoinRequest requestJoin(Long teamId, Long playerId);
    JoinRequest invitePlayer(Long teamId, Long playerId);
    List<JoinRequest> getPendingRequestsForTeam(Long teamId);
    List<JoinRequest> getInvitationsForPlayer(Long userId);
    void acceptRequest(Long requestId);
    void refuseRequest(Long requestId);

    Team transferCaptain(Long teamId, Long newCaptainId);
    void removePlayer(Long teamId, Long userId);
    List<User> getAllPlayers();
}
