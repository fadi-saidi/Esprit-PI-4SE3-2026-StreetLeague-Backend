package tn.esprit.pi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import tn.esprit.pi.config.VirtualTeamConfig;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.OwnedPlayerResponse;
import tn.esprit.pi.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnedPlayerServiceImpl implements IOwnedPlayerService {

    private final OwnedPlayerRepository ownedPlayerRepository;
    private final VirtualTeamRepository teamRepository;
    private final UserRepository userRepository;
    private final PlayerProfileRepository playerRepository;

    @Override
    public OwnedPlayerResponse addPlayerToTeam(Long userId, Long teamId, Long playerProfileId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        // check if user is enabled/active
        if (user.getEnabled() == null || !user.getEnabled()) {
            throw new RuntimeException("User is inactive or signed out");
        }

        VirtualTeam team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found: " + teamId));

        PlayerProfile playerProfile = playerRepository.findById(playerProfileId)
                .orElseThrow(() -> new RuntimeException("PlayerProfile not found: " + playerProfileId));

        boolean exists = ownedPlayerRepository.findByUserIdAndVirtualTeamId(userId, teamId).isPresent();
        if (exists) {
            throw new RuntimeException("Player already assigned to this team");
        }

        SportType sport = team.getSportType();

        long startersCount = ownedPlayerRepository.countByVirtualTeamIdAndStatus(teamId, PlayerStatus.TITULAIRE);
        long subsCount = ownedPlayerRepository.countByVirtualTeamIdAndStatus(teamId, PlayerStatus.REMPLACANT);

        PlayerStatus status;
        if (startersCount < VirtualTeamConfig.MAX_STARTERS.get(sport)) {
            status = PlayerStatus.TITULAIRE;
        } else if (subsCount < VirtualTeamConfig.MAX_SUBSTITUTES.get(sport)) {
            status = PlayerStatus.REMPLACANT;
        } else {
            throw new RuntimeException("Cannot add more players, team is full");
        }

        OwnedPlayer ownedPlayer = new OwnedPlayer();
        ownedPlayer.setUser(user);
        ownedPlayer.setVirtualTeam(team);
        ownedPlayer.setPlayerProfile(playerProfile);
        ownedPlayer.setStatus(status);
        ownedPlayer.setAcquiredDate(LocalDate.now());

        return toResponse(ownedPlayerRepository.save(ownedPlayer));
    }

    @Override
    public void removePlayerFromTeam(Long ownedPlayerId) {
        OwnedPlayer ownedPlayer = ownedPlayerRepository.findById(ownedPlayerId)
                .orElseThrow(() -> new RuntimeException("OwnedPlayer not found: " + ownedPlayerId));
        ownedPlayerRepository.delete(ownedPlayer);
    }

    @Override
    public List<OwnedPlayerResponse> getPlayersOfTeam(Long teamId) {
        return ownedPlayerRepository.findByVirtualTeamId(teamId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OwnedPlayerResponse> getPlayersOfUser(Long userId) {
        return ownedPlayerRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OwnedPlayerResponse getOwnedPlayerById(Long id) {
        return toResponse(ownedPlayerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OwnedPlayer not found: " + id)));
    }

    private OwnedPlayerResponse toResponse(OwnedPlayer op) {
        return OwnedPlayerResponse.builder()
                .id(op.getId())
                .userId(op.getUser().getId())
                .teamId(op.getVirtualTeam().getId())
                .playerProfileId(op.getPlayerProfile().getId())
                .status(op.getStatus())
                .acquiredDate(op.getAcquiredDate())
                .build();
    }
}