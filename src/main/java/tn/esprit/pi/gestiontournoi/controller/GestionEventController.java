package tn.esprit.pi.gestiontournoi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.gestiontournoi.entity.GestionEvent;
import tn.esprit.pi.gestiontournoi.repository.GestionEventRepository;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@RequiredArgsConstructor
public class GestionEventController {

    private final GestionEventRepository repository;

    @GetMapping
    public List<GestionEvent> getAll() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @PostMapping
    public GestionEvent create(@Valid @RequestBody GestionEvent request) {
        request.setId(null);
        return repository.save(request);
    }

    @PutMapping("/{id}")
    public GestionEvent update(@PathVariable Long id, @Valid @RequestBody GestionEvent request) {
        GestionEvent existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setDate(request.getDate());
        existing.setLocation(request.getLocation());
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Event not found");
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
