package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.domain.SportType;
import tn.esprit.pi.dto.PlayerProfileFantasyDto;
import tn.esprit.pi.repository.PlayerProfileRepository;
import tn.esprit.pi.domain.PlayerProfile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/player-profiles")
@RequiredArgsConstructor
public class PlayerProfileController {

    private final PlayerProfileRepository playerProfileRepository;

    @GetMapping
    public List<PlayerProfileFantasyDto> getAll() {
        return playerProfileRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/sport/{sport}")
    public List<PlayerProfileFantasyDto> getBySport(@PathVariable String sport) {
        return playerProfileRepository
                .findBySportType(SportType.valueOf(sport.toUpperCase()))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public PlayerProfileFantasyDto getById(@PathVariable Long id) {
        PlayerProfile pp = playerProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PlayerProfile not found: " + id));
        return toDto(pp);
    }

    private PlayerProfileFantasyDto toDto(PlayerProfile pp) {
        String username  = (pp.getUser() != null && pp.getUser().getUsername() != null)
                ? pp.getUser().getUsername() : "Unknown";
        String[] parts   = username.trim().split("\\s+", 2);
        String firstName = parts[0];
        String lastName  = parts.length > 1 ? parts[1] : "";

        return PlayerProfileFantasyDto.builder()
                .id(pp.getId())
                .firstName(firstName)
                .lastName(lastName)
                .position(pp.getPosition())
                .sportType(pp.getSportType()    != null ? pp.getSportType().name()  : null)
                .level(pp.getLevel()            != null ? pp.getLevel().name()      : null)
                .avgRating(pp.getAvgRating()    != null ? pp.getAvgRating()         : 0.0)
                .fantasyPoints(pp.getFantasyPoints() != null ? pp.getFantasyPoints(): 0)
                .goalsScored(pp.getGoalsScored() != null ? pp.getGoalsScored()      : 0)
                .assists(pp.getAssists()         != null ? pp.getAssists()          : 0)
                .matchesPlayed(pp.getMatchesPlayed() != null ? pp.getMatchesPlayed(): 0)
                .build();
    }
}