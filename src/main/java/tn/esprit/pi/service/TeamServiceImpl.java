package tn.esprit.pi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.repository.*;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamServiceImpl implements ITeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final JoinRequestRepository joinRequestRepository;

    @Override
    public Team createTeam(Team team) {
        Team saved = teamRepository.save(team);
        if (saved.getCaptainId() != null) {
            playerProfileRepository.findByUserId(saved.getCaptainId()).ifPresent(profile -> {
                if (saved.getPlayerProfiles() == null) {
                    saved.setPlayerProfiles(new java.util.HashSet<>());
                }
                if (!saved.getPlayerProfiles().contains(profile)) {
                    saved.getPlayerProfiles().add(profile);
                    teamRepository.save(saved);
                }
            });
        }
        return saved;
    }

    @Override
    public Team updateTeam(Long id, Team team) {
        Team existing = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        existing.setName(team.getName());
        if (team.getLogo() != null && !team.getLogo().isBlank()) {
            existing.setLogo(team.getLogo());
        }
        existing.setSportType(team.getSportType());
        return teamRepository.save(existing);
    }

    @Override
    public Team getTeamById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));
    }

    @Override
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    @Override
    public void deleteTeam(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        // Remove all player memberships (owning side)
        if (team.getPlayerProfiles() != null) {
            team.getPlayerProfiles().clear();
            teamRepository.save(team);
        }
        // Delete all join requests
        joinRequestRepository.deleteByTeam(team);
        teamRepository.delete(team);
    }

    @Override
    public List<Team> getTeamsByUserId(Long userId) {
        return playerProfileRepository.findByUserId(userId)
                .map(p -> p.getTeams() != null ? p.getTeams().stream().toList() : List.<Team>of())
                .orElse(List.of());
    }

    @Override
    public JoinRequest requestJoin(Long teamId, Long playerId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        User player = userRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        boolean alreadyMember = playerProfileRepository.findByUserId(playerId)
                .map(p -> p.getTeams() != null && p.getTeams().stream().anyMatch(t -> t.getId().equals(teamId)))
                .orElse(false);
        if (alreadyMember) throw new RuntimeException("Already a member of this team");

        boolean alreadyRequested = joinRequestRepository
                .existsByTeamAndPlayerAndStatusAndType(team, player, "PENDING", "REQUEST");
        if (alreadyRequested) throw new RuntimeException("Request already sent");

        return joinRequestRepository.save(JoinRequest.builder()
                .team(team).player(player).type("REQUEST").status("PENDING").build());
    }

    @Override
    public JoinRequest invitePlayer(Long teamId, Long playerId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        User player = userRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        boolean alreadyMember = playerProfileRepository.findByUserId(playerId)
                .map(p -> p.getTeams() != null && p.getTeams().stream().anyMatch(t -> t.getId().equals(teamId)))
                .orElse(false);
        if (alreadyMember) throw new RuntimeException("Already a member of this team");

        boolean alreadyInvited = joinRequestRepository
                .existsByTeamAndPlayerAndStatusAndType(team, player, "PENDING", "INVITATION");
        if (alreadyInvited) throw new RuntimeException("Invitation already sent");

        return joinRequestRepository.save(JoinRequest.builder()
                .team(team).player(player).type("INVITATION").status("PENDING").build());
    }

    @Override
    public List<JoinRequest> getPendingRequestsForTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        return joinRequestRepository.findByTeamAndStatusAndType(team, "PENDING", "REQUEST");
    }

    @Override
    public List<JoinRequest> getInvitationsForPlayer(Long userId) {
        User player = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return joinRequestRepository.findByPlayerAndStatusAndType(player, "PENDING", "INVITATION");
    }

    @Override
    public void acceptRequest(Long requestId) {
        JoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus("ACCEPTED");
        joinRequestRepository.save(request);

        Long playerId = request.getPlayer().getId();
        Team team = request.getTeam();
        playerProfileRepository.findByUserId(playerId).ifPresent(profile -> {
            if (team.getPlayerProfiles() == null) {
                team.setPlayerProfiles(new java.util.HashSet<>());
            }
            if (!team.getPlayerProfiles().contains(profile)) {
                team.getPlayerProfiles().add(profile);
                teamRepository.save(team);
            }
        });
    }

    @Override
    public void refuseRequest(Long requestId) {
        JoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus("REFUSED");
        joinRequestRepository.save(request);
    }

    @Override
    public Team transferCaptain(Long teamId, Long newCaptainId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        boolean isMember = playerProfileRepository.findByUserId(newCaptainId)
                .map(p -> p.getTeams() != null && p.getTeams().stream().anyMatch(t -> t.getId().equals(teamId)))
                .orElse(false);
        if (!isMember) throw new RuntimeException("Player is not a member of this team");
        team.setCaptainId(newCaptainId);
        return teamRepository.save(team);
    }

    @Override
    public void removePlayer(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        playerProfileRepository.findByUserId(userId).ifPresent(profile -> {
            if (team.getPlayerProfiles() != null) {
                team.getPlayerProfiles().removeIf(p -> p.getId().equals(profile.getId()));
                teamRepository.save(team);
            }
        });
    }

    @Override
    public List<User> getAllPlayers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.PLAYER)
                .toList();
    }
}
