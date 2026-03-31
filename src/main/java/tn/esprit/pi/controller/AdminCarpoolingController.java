package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.dto.DriverWithCarsAndCarpoolingsDTO;
import tn.esprit.pi.service.IAdminCarpoolingService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/carpoolings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCarpoolingController {

    private final IAdminCarpoolingService adminCarpoolingService;

    // GET /api/admin/carpoolings/drivers
    // Voir tous les drivers avec leurs voitures et carpoolings
    @GetMapping("/drivers")
    public ResponseEntity<List<DriverWithCarsAndCarpoolingsDTO>> getAllDrivers() {
        return ResponseEntity.ok(adminCarpoolingService.getAllDriversWithCarsAndCarpoolings());
    }

    // GET /api/admin/carpoolings/drivers/{driverId}
    // Voir un driver specifique avec ses voitures et carpoolings
    @GetMapping("/drivers/{driverId}")
    public ResponseEntity<DriverWithCarsAndCarpoolingsDTO> getDriver(@PathVariable Long driverId) {
        return ResponseEntity.ok(adminCarpoolingService.getDriverWithCarsAndCarpoolings(driverId));
    }

    // DELETE /api/admin/carpoolings/cars/{carId}
    @DeleteMapping("/cars/{carId}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long carId) {
        adminCarpoolingService.deleteCar(carId);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/admin/carpoolings/{carpoolingId}
    @DeleteMapping("/{carpoolingId}")
    public ResponseEntity<Void> deleteCarpooling(@PathVariable Long carpoolingId) {
        adminCarpoolingService.deleteCarpooling(carpoolingId);
        return ResponseEntity.noContent().build();
    }
}