package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi.domain.Car;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    // Toutes les voitures d'un driver
    List<Car> findByDriverId(Long driverId);

    // Verifier que la voiture appartient au driver
    Optional<Car> findByIdAndDriverId(Long carId, Long driverId);
}