package tn.esprit.pi.controller;

import tn.esprit.pi.dto.MatchDTO;
import tn.esprit.pi.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MatchController {

    private final MatchService matchService;

    @GetMapping
    public ResponseEntity<List<MatchDTO>> getAll() {
        return ResponseEntity.ok(matchService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.findById(id));
    }

    @GetMapping("/tournoi/{tournoiId}")
    public ResponseEntity<List<MatchDTO>> getByTournoi(@PathVariable Long tournoiId) {
        return ResponseEntity.ok(matchService.findByTournoi(tournoiId));
    }

    @PostMapping
    public ResponseEntity<MatchDTO> create(@Valid @RequestBody MatchDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matchService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MatchDTO> update(@PathVariable Long id, @Valid @RequestBody MatchDTO dto) {
        return ResponseEntity.ok(matchService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        matchService.delete(id);
        return ResponseEntity.noContent().build();
    }
}