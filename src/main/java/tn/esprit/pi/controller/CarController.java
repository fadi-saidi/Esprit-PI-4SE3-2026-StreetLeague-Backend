package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.dto.CarDTO;
import tn.esprit.pi.service.ICarService;

import java.util.List;

@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarController {

    private final ICarService carService;

    private String getConnectedEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // POST /api/cars/add
    @PostMapping("/add")
    public ResponseEntity<CarDTO> addCar(@RequestBody CarDTO dto) {
        return ResponseEntity.ok(carService.addCar(dto, getConnectedEmail()));
    }

    // GET /api/cars/all
    @GetMapping("/all")
    public ResponseEntity<List<CarDTO>> getAllCars() {
        return ResponseEntity.ok(carService.getAllCars());
    }

    // GET /api/cars/my-cars
    @GetMapping("/my-cars")
    public ResponseEntity<List<CarDTO>> getMyCars() {
        return ResponseEntity.ok(carService.getMyCars(getConnectedEmail()));
    }

    // GET /api/cars/details/{id}
    @GetMapping("/details/{id}")
    public ResponseEntity<CarDTO> getCarById(@PathVariable Long id) {
        return ResponseEntity.ok(carService.getCarById(id));
    }

    // DELETE /api/cars/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        carService.deleteCar(id, getConnectedEmail());
        return ResponseEntity.noContent().build();
    }
}