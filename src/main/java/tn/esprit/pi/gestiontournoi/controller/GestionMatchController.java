package tn.esprit.pi.gestiontournoi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.gestiontournoi.entity.GestionMatch;
import tn.esprit.pi.gestiontournoi.repository.GestionMatchRepository;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@RequiredArgsConstructor
public class GestionMatchController {

    private final GestionMatchRepository repository;

    @GetMapping
    public List<GestionMatch> getAll() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @PostMapping
    public GestionMatch create(@Valid @RequestBody GestionMatch request) {
        request.setId(null);
        return repository.save(request);
    }

    @PutMapping("/{id}")
    public GestionMatch update(@PathVariable Long id, @Valid @RequestBody GestionMatch request) {
        GestionMatch existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
        existing.setHomeTeam(request.getHomeTeam());
        existing.setAwayTeam(request.getAwayTeam());
        existing.setDate(request.getDate());
        existing.setScore(request.getScore());
        existing.setLocation(request.getLocation());
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Match not found");
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
