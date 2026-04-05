package tn.esprit.pi.gestiontournoi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.gestiontournoi.entity.GestionTournament;
import tn.esprit.pi.gestiontournoi.repository.GestionTournamentRepository;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@RequiredArgsConstructor
public class GestionTournamentController {

    private final GestionTournamentRepository repository;

    @GetMapping
    public List<GestionTournament> getAll() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @PostMapping
    public GestionTournament create(@Valid @RequestBody GestionTournament request) {
        request.setId(null);
        return repository.save(request);
    }

    @PutMapping("/{id}")
    public GestionTournament update(@PathVariable Long id, @Valid @RequestBody GestionTournament request) {
        GestionTournament existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
        existing.setName(request.getName());
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        existing.setLocation(request.getLocation());
        existing.setMaxTeams(request.getMaxTeams());
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Tournament not found");
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
