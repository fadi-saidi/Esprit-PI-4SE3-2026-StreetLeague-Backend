package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.dto.OwnedPlayerResponse;
import tn.esprit.pi.service.IOwnedPlayerService;

import java.util.List;

@RestController
@RequestMapping("/owned-players")
@RequiredArgsConstructor
public class OwnedPlayerController {

    private final IOwnedPlayerService ownedPlayerService;

    @PostMapping("/add")
    public OwnedPlayerResponse addPlayerToTeam(
            @RequestParam Long userId,
            @RequestParam Long teamId,
            @RequestParam Long playerProfileId) {
        return ownedPlayerService.addPlayerToTeam(userId, teamId, playerProfileId);
    }

    @DeleteMapping("/remove/{ownedPlayerId}")
    public void removePlayerFromTeam(@PathVariable Long ownedPlayerId) {
        ownedPlayerService.removePlayerFromTeam(ownedPlayerId);
    }

    @GetMapping("/team/{teamId}")
    public List<OwnedPlayerResponse> getPlayersOfTeam(@PathVariable Long teamId) {
        return ownedPlayerService.getPlayersOfTeam(teamId);
    }

    @GetMapping("/user/{userId}")
    public List<OwnedPlayerResponse> getPlayersOfUser(@PathVariable Long userId) {
        return ownedPlayerService.getPlayersOfUser(userId);
    }

    @GetMapping("/{id}")
    public OwnedPlayerResponse getOwnedPlayerById(@PathVariable Long id) {
        return ownedPlayerService.getOwnedPlayerById(id);
    }
}
