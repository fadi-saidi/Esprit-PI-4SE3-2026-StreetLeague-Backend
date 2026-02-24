package tn.esprit.pi.service;

import tn.esprit.pi.dto.OwnedPlayerResponse;

import java.util.List;

public interface IOwnedPlayerService {

    OwnedPlayerResponse addPlayerToTeam(Long userId, Long teamId, Long playerProfileId);

    void removePlayerFromTeam(Long ownedPlayerId);

    List<OwnedPlayerResponse> getPlayersOfTeam(Long teamId);

    List<OwnedPlayerResponse> getPlayersOfUser(Long userId);

    OwnedPlayerResponse getOwnedPlayerById(Long id);
}