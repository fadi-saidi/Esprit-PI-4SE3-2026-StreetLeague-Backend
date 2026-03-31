package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.dto.CarpoolingDTO;
import tn.esprit.pi.service.ICarpoolingService;

import java.util.List;

@RestController
@RequestMapping("/carpoolings")
@RequiredArgsConstructor
public class CarpoolingController {

    private final ICarpoolingService carpoolingService;

    private String getConnectedEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // POST /api/carpoolings/create
    @PostMapping("/create")
    public ResponseEntity<CarpoolingDTO> create(@Valid @RequestBody CarpoolingDTO dto) {
        return ResponseEntity.ok(carpoolingService.createCarpooling(dto, getConnectedEmail()));
    }

    // GET /api/carpoolings/all
    @GetMapping("/all")
    public ResponseEntity<List<CarpoolingDTO>> getAll() {
        return ResponseEntity.ok(carpoolingService.getAllCarpoolings());
    }

    // GET /api/carpoolings/my-trips (trajets que j'ai crees en tant que driver)
    @GetMapping("/my-trips")
    public ResponseEntity<List<CarpoolingDTO>> getMyCreatedTrips() {
        return ResponseEntity.ok(carpoolingService.getMyCreatedCarpoolings(getConnectedEmail()));
    }

    // GET /api/carpoolings/my-joined (trajets auxquels je participe)
    @GetMapping("/my-joined")
    public ResponseEntity<List<CarpoolingDTO>> getMyJoinedTrips() {
        return ResponseEntity.ok(carpoolingService.getMyJoinedCarpoolings(getConnectedEmail()));
    }

    // GET /api/carpoolings/details/{id}
    @GetMapping("/details/{id}")
    public ResponseEntity<CarpoolingDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(carpoolingService.getCarpoolingById(id));
    }

    // PUT /api/carpoolings/{id}/join
    @PutMapping("/{id}/join")
    public ResponseEntity<CarpoolingDTO> join(@PathVariable Long id) {
        return ResponseEntity.ok(carpoolingService.joinCarpooling(id, getConnectedEmail()));
    }

    // PUT /api/carpoolings/{id}/leave
    @PutMapping("/{id}/leave")
    public ResponseEntity<CarpoolingDTO> leave(@PathVariable Long id) {
        return ResponseEntity.ok(carpoolingService.leaveCarpooling(id, getConnectedEmail()));
    }

    // DELETE /api/carpoolings/{id}/delete
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        carpoolingService.deleteCarpooling(id, getConnectedEmail());
        return ResponseEntity.noContent().build();
    }
}