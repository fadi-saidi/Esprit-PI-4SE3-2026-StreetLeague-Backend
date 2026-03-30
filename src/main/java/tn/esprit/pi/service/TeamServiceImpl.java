package tn.esprit.pi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.JoinRequestDTO;
import tn.esprit.pi.dto.PlayerSummaryDTO;
import tn.esprit.pi.dto.TeamDTO;
import tn.esprit.pi.repository.*;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamServiceImpl implements ITeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final JoinRequestRepository joinRequestRepository;

    // ── Mapping helpers ───────────────────────────────────────────────────────

    private PlayerSummaryDTO toPlayerDTO(PlayerProfile p) {
        if (p == null) return null;
        User u = p.getUser();
        return PlayerSummaryDTO.builder()
                .id(u != null ? u.getId() : null)
                .fullName(u != null ? u.getUsername() : null)
                .email(u != null ? u.getEmail() : null)
                .level(p.getLevel() != null ? p.getLevel().name() : null)
                .build();
    }

    private TeamDTO toTeamDTO(Team t) {
        List<PlayerSummaryDTO> players = t.getPlayerProfiles() == null ? List.of()
                : t.getPlayerProfiles().stream()
                        .map(this::toPlayerDTO)
                        .collect(Collectors.toList());

        String captainName = null;
        if (t.getCaptainId() != null) {
            captainName = userRepository.findById(t.getCaptainId())
                    .map(User::getUsername).orElse(null);
        }

        String coachName = null;
        Long   coachId   = null;
        if (t.getCoachProfile() != null && t.getCoachProfile().getUser() != null) {
            coachId   = t.getCoachProfile().getUser().getId();
            coachName = t.getCoachProfile().getUser().getUsername();
        }

        return TeamDTO.builder()
                .id(t.getId())
                .name(t.getName())
                .logo(t.getLogo())
                .type(t.getSportType() != null ? t.getSportType().name() : null)
                .captainId(t.getCaptainId())
                .captainName(captainName)
                .coachId(coachId)
                .coachName(coachName)
                .players(players)
                .playerCount(players.size())
                .createdAt(t.getCreatedAt())
                .build();
    }

    private JoinRequestDTO toJoinRequestDTO(JoinRequest jr) {
        JoinRequestDTO.TeamRef teamRef = null;
        if (jr.getTeam() != null) {
            Team t = jr.getTeam();
            teamRef = JoinRequestDTO.TeamRef.builder()
                    .id(t.getId())
                    .name(t.getName())
                    .type(t.getSportType() != null ? t.getSportType().name() : null)
                    .captainId(t.getCaptainId())
                    .build();
        }

        PlayerSummaryDTO playerDTO = null;
        if (jr.getPlayer() != null) {
            User u = jr.getPlayer();
            playerDTO = PlayerSummaryDTO.builder()
                    .id(u.getId())
                    .fullName(u.getUsername())
                    .email(u.getEmail())
                    .build();
        }

        return JoinRequestDTO.builder()
                .id(jr.getId())
                .team(teamRef)
                .player(playerDTO)
                .type(jr.getType())
                .status(jr.getStatus())
                .build();
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Override
    public TeamDTO createTeam(TeamDTO dto) {
        Team team = new Team();
        team.setName(dto.getName());
        team.setLogo(dto.getLogo());
        if (dto.getType() != null && !dto.getType().isBlank()) {
            team.setSportType(SportType.valueOf(dto.getType()));
        }
        team.setCaptainId(dto.getCaptainId());
        team.setCreatedAt(java.time.LocalDateTime.now());

        Team saved = teamRepository.save(team);

        // Auto-add captain as first player
        if (saved.getCaptainId() != null) {
            playerProfileRepository.findByUserId(saved.getCaptainId()).ifPresent(profile -> {
                if (saved.getPlayerProfiles() == null) saved.setPlayerProfiles(new HashSet<>());
                saved.getPlayerProfiles().add(profile);
                teamRepository.save(saved);
            });
        }

        return toTeamDTO(saved);
    }

    @Override
    public TeamDTO updateTeam(Long id, TeamDTO dto) {
        Team existing = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        existing.setName(dto.getName());
        if (dto.getLogo() != null && !dto.getLogo().isBlank()) {
            existing.setLogo(dto.getLogo());
        }
        if (dto.getType() != null && !dto.getType().isBlank()) {
            existing.setSportType(SportType.valueOf(dto.getType()));
        }
        return toTeamDTO(teamRepository.save(existing));
    }

    @Override
    public TeamDTO getTeamById(Long id) {
        return toTeamDTO(teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found")));
    }

    @Override
    public List<TeamDTO> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(this::toTeamDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTeam(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        if (team.getPlayerProfiles() != null) {
            team.getPlayerProfiles().clear();
            teamRepository.save(team);
        }
        joinRequestRepository.deleteByTeam(team);
        teamRepository.delete(team);
    }

    @Override
    public List<TeamDTO> getTeamsByUserId(Long userId) {
        return playerProfileRepository.findByUserId(userId)
                .map(p -> p.getTeams() == null ? List.<Team>of() : p.getTeams().stream().toList())
                .orElse(List.of())
                .stream().map(this::toTeamDTO).collect(Collectors.toList());
    }

    // ── Join requests ─────────────────────────────────────────────────────────

    @Override
    public JoinRequestDTO requestJoin(Long teamId, Long playerId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        User player = userRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        boolean alreadyMember = playerProfileRepository.findByUserId(playerId)
                .map(p -> p.getTeams() != null && p.getTeams().stream().anyMatch(t -> t.getId().equals(teamId)))
                .orElse(false);
        if (alreadyMember) throw new RuntimeException("Already a member of this team");

        if (joinRequestRepository.existsByTeamAndPlayerAndStatusAndType(team, player, "PENDING", "REQUEST"))
            throw new RuntimeException("Request already sent");

        return toJoinRequestDTO(joinRequestRepository.save(
                JoinRequest.builder().team(team).player(player).type("REQUEST").status("PENDING").build()));
    }

    @Override
    public JoinRequestDTO invitePlayer(Long teamId, Long playerId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        User player = userRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        boolean alreadyMember = playerProfileRepository.findByUserId(playerId)
                .map(p -> p.getTeams() != null && p.getTeams().stream().anyMatch(t -> t.getId().equals(teamId)))
                .orElse(false);
        if (alreadyMember) throw new RuntimeException("Already a member of this team");

        if (joinRequestRepository.existsByTeamAndPlayerAndStatusAndType(team, player, "PENDING", "INVITATION"))
            throw new RuntimeException("Invitation already sent");

        return toJoinRequestDTO(joinRequestRepository.save(
                JoinRequest.builder().team(team).player(player).type("INVITATION").status("PENDING").build()));
    }

    @Override
    public List<JoinRequestDTO> getPendingRequestsForTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        return joinRequestRepository.findByTeamAndStatusAndType(team, "PENDING", "REQUEST")
                .stream().map(this::toJoinRequestDTO).collect(Collectors.toList());
    }

    @Override
    public List<JoinRequestDTO> getInvitationsForPlayer(Long userId) {
        User player = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return joinRequestRepository.findByPlayerAndStatusAndType(player, "PENDING", "INVITATION")
                .stream().map(this::toJoinRequestDTO).collect(Collectors.toList());
    }

    @Override
    public void acceptRequest(Long requestId) {
        JoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus("ACCEPTED");
        joinRequestRepository.save(request);

        Team team = request.getTeam();
        playerProfileRepository.findByUserId(request.getPlayer().getId()).ifPresent(profile -> {
            if (team.getPlayerProfiles() == null) team.setPlayerProfiles(new HashSet<>());
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

    // ── Captain management ────────────────────────────────────────────────────

    @Override
    public TeamDTO transferCaptain(Long teamId, Long newCaptainId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        boolean isMember = playerProfileRepository.findByUserId(newCaptainId)
                .map(p -> p.getTeams() != null && p.getTeams().stream().anyMatch(t -> t.getId().equals(teamId)))
                .orElse(false);
        if (!isMember) throw new RuntimeException("Player is not a member of this team");
        team.setCaptainId(newCaptainId);
        return toTeamDTO(teamRepository.save(team));
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

    // ── Players list ──────────────────────────────────────────────────────────

    @Override
    public List<PlayerSummaryDTO> getAllPlayers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.PLAYER)
                .map(u -> PlayerSummaryDTO.builder()
                        .id(u.getId())
                        .fullName(u.getUsername())
                        .email(u.getEmail())
                        .build())
                .collect(Collectors.toList());
    }
}
