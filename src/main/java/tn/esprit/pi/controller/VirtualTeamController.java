package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.dto.VirtualTeamDto;
import tn.esprit.pi.dto.VirtualTeamResponse;
import tn.esprit.pi.service.IVirtualTeamService;

import java.util.List;

@RestController
@RequestMapping("/virtual-teams")
@RequiredArgsConstructor
public class VirtualTeamController {

    private final IVirtualTeamService teamService;

    @PostMapping
    public VirtualTeamResponse createTeam(@RequestBody VirtualTeamDto request) {
        return teamService.createVirtualTeam(request);
    }

    @PutMapping("/{id}")
    public VirtualTeamResponse updateTeam(@PathVariable Long id, @RequestBody VirtualTeamDto request) {
        return teamService.updateVirtualTeam(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteTeam(@PathVariable Long id) {
        teamService.deleteVirtualTeam(id);
    }

    @GetMapping("/{id}")
    public VirtualTeamResponse getTeam(@PathVariable Long id) {
        return teamService.getVirtualTeamById(id);
    }

    @GetMapping
    public List<VirtualTeamResponse> getAllTeams() {
        return teamService.getAllTeams();
    }

    @GetMapping("/user/{userId}")
    public List<VirtualTeamResponse> getTeamsByUser(@PathVariable Long userId) {
        return teamService.getTeamsByUser(userId);
    }
}