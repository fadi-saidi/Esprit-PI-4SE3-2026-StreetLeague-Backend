package tn.esprit.pi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.Team;
import tn.esprit.pi.repository.TeamRepository;
import tn.esprit.pi.service.ITeamService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements ITeamService {

    private final TeamRepository teamRepository;

    @Override
    public Team createTeam(Team team) {
        team.setCreatedAt(LocalDateTime.now());
        return teamRepository.save(team);
    }

    @Override
    public Team updateTeam(Long id, Team team) {
        Team existingTeam = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        existingTeam.setName(team.getName());
        existingTeam.setLogo(team.getLogo());
        existingTeam.setId_capitaine(team.getId_capitaine());
        existingTeam.setSportType(team.getSportType());
        existingTeam.setCoachProfile(team.getCoachProfile());
        existingTeam.setPlayerProfiles(team.getPlayerProfiles());

        return teamRepository.save(existingTeam);
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
        teamRepository.deleteById(id);
    }
}