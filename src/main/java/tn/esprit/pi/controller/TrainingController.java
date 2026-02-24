package tn.esprit.pi.controller;

import tn.esprit.pi.dto.TrainingDTO;
import tn.esprit.pi.service.TrainingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TrainingController {

    private final TrainingService trainingService;

    @GetMapping
    public ResponseEntity<List<TrainingDTO>> getAll() {
        return ResponseEntity.ok(trainingService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainingDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(trainingService.findById(id));
    }

    @GetMapping("/tournoi/{tournoiId}")
    public ResponseEntity<List<TrainingDTO>> getByTournoi(@PathVariable Long tournoiId) {
        return ResponseEntity.ok(trainingService.findByTournoi(tournoiId));
    }

    @PostMapping
    public ResponseEntity<TrainingDTO> create(@Valid @RequestBody TrainingDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainingService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainingDTO> update(@PathVariable Long id, @Valid @RequestBody TrainingDTO dto) {
        return ResponseEntity.ok(trainingService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        trainingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
