package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.domain.JoinRequest;
import tn.esprit.pi.domain.SportType;
import tn.esprit.pi.domain.Team;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.service.ITeamService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@CrossOrigin("*")
public class TeamController {

    private final ITeamService teamService;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @GetMapping
    public List<Team> getAllTeams() {
        return teamService.getAllTeams();
    }

    @GetMapping("/{id}")
    public Team getTeamById(@PathVariable Long id) {
        return teamService.getTeamById(id);
    }

    @PostMapping
    public Team createTeam(@RequestBody Map<String, Object> body) {
        validateTeam(body);
        Team team = mapToTeam(body);
        team.setCaptainId(body.containsKey("captainId") ? Long.valueOf(body.get("captainId").toString()) : null);
        team.setCreatedAt(LocalDateTime.now());
        return teamService.createTeam(team);
    }

    @PutMapping("/{id}")
    public Team updateTeam(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        validateTeam(body);
        return teamService.updateTeam(id, mapToTeam(body));
    }

    private Team mapToTeam(Map<String, Object> body) {
        Team team = new Team();
        if (body.containsKey("name"))  team.setName(body.get("name").toString());
        if (body.containsKey("logo"))  team.setLogo(body.get("logo").toString());
        String typeStr = body.containsKey("type") ? body.get("type").toString()
                       : body.containsKey("sportType") ? body.get("sportType").toString() : null;
        if (typeStr != null && !typeStr.isBlank()) {
            try { team.setSportType(SportType.valueOf(typeStr)); } catch (IllegalArgumentException ignored) {}
        }
        return team;
    }

    private void validateTeam(Map<String, Object> body) {
        String name = body.containsKey("name") ? body.get("name").toString().trim() : "";
        if (name.isBlank()) throw new IllegalArgumentException("Team name is required");
        if (name.length() < 2) throw new IllegalArgumentException("Team name must be at least 2 characters");
        if (name.length() > 80) throw new IllegalArgumentException("Team name cannot exceed 80 characters");

        String typeStr = body.containsKey("type") ? body.get("type").toString()
                       : body.containsKey("sportType") ? body.get("sportType").toString() : "";
        if (typeStr == null || typeStr.isBlank()) throw new IllegalArgumentException("Sport type is required");
        try { SportType.valueOf(typeStr); } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid sport type: " + typeStr);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    // ── My teams ──────────────────────────────────────────────────────────────

    @GetMapping("/my/{userId}")
    public List<Team> getMyTeams(@PathVariable Long userId) {
        return teamService.getTeamsByUserId(userId);
    }

    // ── Join requests ─────────────────────────────────────────────────────────

    @PostMapping("/{teamId}/request/{playerId}")
    public JoinRequest requestJoin(@PathVariable Long teamId, @PathVariable Long playerId) {
        return teamService.requestJoin(teamId, playerId);
    }

    @PostMapping("/{teamId}/invite/{playerId}")
    public JoinRequest invitePlayer(@PathVariable Long teamId, @PathVariable Long playerId) {
        return teamService.invitePlayer(teamId, playerId);
    }

    @GetMapping("/{teamId}/requests")
    public List<JoinRequest> getPendingRequests(@PathVariable Long teamId) {
        return teamService.getPendingRequestsForTeam(teamId);
    }

    @GetMapping("/invitations/{userId}")
    public List<JoinRequest> getMyInvitations(@PathVariable Long userId) {
        return teamService.getInvitationsForPlayer(userId);
    }

    @PutMapping("/requests/{requestId}/accept")
    public ResponseEntity<Void> acceptRequest(@PathVariable Long requestId) {
        teamService.acceptRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/requests/{requestId}/refuse")
    public ResponseEntity<Void> refuseRequest(@PathVariable Long requestId) {
        teamService.refuseRequest(requestId);
        return ResponseEntity.ok().build();
    }

    // ── Captain management ────────────────────────────────────────────────────

    @PutMapping("/{teamId}/captain/{newCaptainId}")
    public Team transferCaptain(@PathVariable Long teamId, @PathVariable Long newCaptainId) {
        return teamService.transferCaptain(teamId, newCaptainId);
    }

    @DeleteMapping("/{teamId}/players/{userId}")
    public ResponseEntity<Void> removePlayer(@PathVariable Long teamId, @PathVariable Long userId) {
        teamService.removePlayer(teamId, userId);
        return ResponseEntity.noContent().build();
    }

    // ── Players list ──────────────────────────────────────────────────────────

    @GetMapping("/players")
    public List<User> getAllPlayers() {
        return teamService.getAllPlayers();
    }
}
