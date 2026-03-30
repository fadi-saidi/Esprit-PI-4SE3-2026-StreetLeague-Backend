package tn.esprit.pi.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.VirtualTeamDto;
import tn.esprit.pi.dto.VirtualTeamResponse;
import tn.esprit.pi.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VirtualTeamServiceImpl implements IVirtualTeamService {

    private final VirtualTeamRepository teamRepository;
    private final UserRepository userRepository;
    private final PredictionRepository predictionRepository;

    @Override
    public VirtualTeamResponse createVirtualTeam(VirtualTeamDto request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        VirtualTeam team = new VirtualTeam();
        team.setName(request.getName());
        team.setSportType(request.getSportType());
        team.setUser(user);
        team.setEarnedPoints(0.0);
        team.setWeekPoints(0.0);
        team.setPlayerIds(request.getPlayerIds()); // ✅ persist player IDs

        return toResponse(teamRepository.save(team));
    }

    @Override
    public VirtualTeamResponse updateVirtualTeam(Long id, VirtualTeamDto request) {

        VirtualTeam existing = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("VirtualTeam not found"));

        existing.setSportType(request.getSportType());
        existing.setName(request.getName());
        existing.setPlayerIds(request.getPlayerIds()); // ✅ update player IDs

        if (request.getEarnedPoints() != null) {
            existing.setEarnedPoints(request.getEarnedPoints());
        }
        if (request.getWeekPoints() != null) {
            existing.setWeekPoints(request.getWeekPoints());
        }

        return toResponse(teamRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteVirtualTeam(Long id) {
        predictionRepository.deleteByVirtualTeamId(id);
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

    // ✅ Mapper — uses playerIds directly from entity
    private VirtualTeamResponse toResponse(VirtualTeam team) {
        return VirtualTeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .sportType(team.getSportType())
                .earnedPoints(team.getEarnedPoints())
                .weekPoints(team.getWeekPoints())
                .userId(team.getUser().getId())
                .playerIds(team.getPlayerIds() != null ? team.getPlayerIds() : List.of())
                .build();
    }
}