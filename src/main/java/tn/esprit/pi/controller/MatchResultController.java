package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.domain.Match;
import tn.esprit.pi.dto.MatchResultDto;
import tn.esprit.pi.repository.MatchRepository;
import tn.esprit.pi.service.MatchService;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("*")
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchResultController {

    private final MatchService    matchService;
    private final MatchRepository matchRepository;

    /** Admin: list all matches (for picking which match to enter results for) */
    @GetMapping
    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    /** Admin: submit match results → auto-fills PlayerStat → auto-resolves predictions */
    @PostMapping("/results")
    public ResponseEntity<Map<String, Object>> submitResults(@RequestBody MatchResultDto dto) {
        return ResponseEntity.ok(matchService.submitMatchResult(dto));
    }
}
