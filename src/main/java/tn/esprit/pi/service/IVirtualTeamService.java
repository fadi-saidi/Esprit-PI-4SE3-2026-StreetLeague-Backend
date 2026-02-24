package tn.esprit.pi.service;

import tn.esprit.pi.dto.VirtualTeamDto;
import tn.esprit.pi.dto.VirtualTeamResponse;

import java.util.List;

public interface IVirtualTeamService {

    VirtualTeamResponse createVirtualTeam(VirtualTeamDto request);

    VirtualTeamResponse updateVirtualTeam(Long id, VirtualTeamDto request);

    void deleteVirtualTeam(Long id);

    VirtualTeamResponse getVirtualTeamById(Long id);

    List<VirtualTeamResponse> getAllTeams();

    List<VirtualTeamResponse> getTeamsByUser(Long userId);

}