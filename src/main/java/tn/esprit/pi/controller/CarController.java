package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.pi.dto.CarDTO;
import tn.esprit.pi.service.ICarService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarController {

    private final ICarService carService;

    private String getConnectedEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PostMapping("/add")
    public ResponseEntity<CarDTO> addCar(@Valid @RequestBody CarDTO dto) {
        return ResponseEntity.ok(carService.addCar(dto, getConnectedEmail()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<CarDTO>> getAllCars() {
        return ResponseEntity.ok(carService.getAllCars());
    }

    @GetMapping("/my-cars")
    public ResponseEntity<List<CarDTO>> getMyCars() {
        return ResponseEntity.ok(carService.getMyCars(getConnectedEmail()));
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<CarDTO> getCarById(@PathVariable Long id) {
        return ResponseEntity.ok(carService.getCarById(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        carService.deleteCar(id, getConnectedEmail());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/upload-photo")
    public ResponseEntity<Map<String, String>> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {

        String extension = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            extension = original.substring(original.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID() + extension;

        Path uploadDir = Paths.get("uploads/cars");
        Files.createDirectories(uploadDir);
        Files.copy(file.getInputStream(), uploadDir.resolve(fileName));

        String photoUrl = "/uploads/cars/" + fileName;
        carService.updatePhotoUrl(id, photoUrl, getConnectedEmail());

        return ResponseEntity.ok(Map.of("photoUrl", photoUrl));
    }
}
