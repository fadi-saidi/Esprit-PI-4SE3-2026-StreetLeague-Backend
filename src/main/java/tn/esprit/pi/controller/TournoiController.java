package tn.esprit.pi.controller;

import tn.esprit.pi.dto.TournoiDTO;
import tn.esprit.pi.service.TournoiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tournois")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TournoiController {

    private final TournoiService tournoiService;

    @GetMapping
    public ResponseEntity<List<TournoiDTO>> getAll() {
        return ResponseEntity.ok(tournoiService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournoiDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tournoiService.findById(id));
    }

    @PostMapping
    public ResponseEntity<TournoiDTO> create(@Valid @RequestBody TournoiDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tournoiService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TournoiDTO> update(@PathVariable Long id, @Valid @RequestBody TournoiDTO dto) {
        return ResponseEntity.ok(tournoiService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tournoiService.delete(id);
        return ResponseEntity.noContent().build();
    }
}