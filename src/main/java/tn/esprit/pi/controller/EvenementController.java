package tn.esprit.pi.controller;

import tn.esprit.pi.dto.EvenementDTO;
import tn.esprit.pi.service.EvenementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evenements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EvenementController {

    private final EvenementService evenementService;

    @GetMapping
    public ResponseEntity<List<EvenementDTO>> getAll() {
        return ResponseEntity.ok(evenementService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvenementDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(evenementService.findById(id));
    }

    @GetMapping("/tournoi/{tournoiId}")
    public ResponseEntity<List<EvenementDTO>> getByTournoi(@PathVariable Long tournoiId) {
        return ResponseEntity.ok(evenementService.findByTournoi(tournoiId));
    }
}