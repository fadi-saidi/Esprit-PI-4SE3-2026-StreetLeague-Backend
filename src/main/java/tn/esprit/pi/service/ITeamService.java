package tn.esprit.pi.service;

import tn.esprit.pi.domain.Team;
import java.util.List;

public interface ITeamService {

    Team createTeam(Team team);

    Team updateTeam(Long id, Team team);

    Team getTeamById(Long id);

    List<Team> getAllTeams();

    void deleteTeam(Long id);
}