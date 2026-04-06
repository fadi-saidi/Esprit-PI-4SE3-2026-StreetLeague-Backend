package tn.esprit.pi.gestiontournoi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.gestiontournoi.entity.GestionTraining;
import tn.esprit.pi.gestiontournoi.repository.GestionTrainingRepository;

import java.util.List;

@RestController
@RequestMapping("/api/trainings")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@RequiredArgsConstructor
public class GestionTrainingController {

    private final GestionTrainingRepository repository;

    @GetMapping
    public List<GestionTraining> getAll() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @PostMapping
    public GestionTraining create(@Valid @RequestBody GestionTraining request) {
        request.setId(null);
        return repository.save(request);
    }

    @PutMapping("/{id}")
    public GestionTraining update(@PathVariable Long id, @Valid @RequestBody GestionTraining request) {
        GestionTraining existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Training not found"));
        existing.setTitle(request.getTitle());
        existing.setCoach(request.getCoach());
        existing.setDate(request.getDate());
        existing.setDuration(request.getDuration());
        existing.setLocation(request.getLocation());
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Training not found");
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
