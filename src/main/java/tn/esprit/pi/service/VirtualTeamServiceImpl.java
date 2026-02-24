package tn.esprit.pi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.VirtualTeamDto;
import tn.esprit.pi.dto.VirtualTeamResponse;
import tn.esprit.pi.repository.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VirtualTeamServiceImpl implements IVirtualTeamService {

    private final VirtualTeamRepository teamRepository;
    private final PlayerProfileRepository playerRepository;
    private final OwnedPlayerRepository ownedPlayerRepository;
    private final UserRepository userRepository;

    @Override
    public VirtualTeamResponse createVirtualTeam(VirtualTeamDto request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        VirtualTeam team = new VirtualTeam();
        team.setSportType(request.getSportType());
        team.setUser(user);
        team.setEarnedPoints(0.0);
        team.setWeekPoints(0.0);

        VirtualTeam savedTeam = teamRepository.save(team);

        Set<OwnedPlayer> ownedPlayers = new HashSet<>();

        for (Long playerId : request.getPlayerIds()) {
            PlayerProfile player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new RuntimeException("Player not found: " + playerId));

            OwnedPlayer op = new OwnedPlayer();
            op.setPlayerProfile(player);
            op.setVirtualTeam(savedTeam);
            op.setStatus(PlayerStatus.TITULAIRE);
            op.setAcquiredDate(java.time.LocalDate.now());

            ownedPlayers.add(op);
        }

        ownedPlayerRepository.saveAll(ownedPlayers);
        savedTeam.setOwnedPlayers(ownedPlayers);

        return toResponse(savedTeam);
    }

    @Override
    public VirtualTeamResponse updateVirtualTeam(Long id, VirtualTeamDto request) {

        VirtualTeam existing = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("VirtualTeam not found"));

        existing.setSportType(request.getSportType());

        if (request.getEarnedPoints() != null) {
            existing.setEarnedPoints(request.getEarnedPoints());
        }
        if (request.getWeekPoints() != null) {
            existing.setWeekPoints(request.getWeekPoints());
        }

        return toResponse(teamRepository.save(existing));
    }

    @Override
    public void deleteVirtualTeam(Long id) {
        teamRepository.deleteById(id);
    }

    @Override
    public VirtualTeamResponse getVirtualTeamById(Long id) {
        return toResponse(teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("VirtualTeam not found")));
    }

    @Override
    public List<VirtualTeamResponse> getAllTeams() {
        return teamRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VirtualTeamResponse> getTeamsByUser(Long userId) {
        return teamRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ✅ Private mapper — keeps entity out of response
    private VirtualTeamResponse toResponse(VirtualTeam team) {
        return VirtualTeamResponse.builder()
                .id(team.getId())
                .sportType(team.getSportType())
                .earnedPoints(team.getEarnedPoints())
                .weekPoints(team.getWeekPoints())
                .userId(team.getUser().getId())
                .build();
    }
}