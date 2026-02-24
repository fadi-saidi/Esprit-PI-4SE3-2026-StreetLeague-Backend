package tn.esprit.pi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.Car;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.dto.CarDTO;
import tn.esprit.pi.repository.CarRepository;
import tn.esprit.pi.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements ICarService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    private CarDTO toDTO(Car car) {
        return CarDTO.builder()
                .id(car.getId())
                .model(car.getModel())
                .seats(car.getSeats())
                .availableSeats(car.getAvailableSeats())
                .plateNumber(car.getPlateNumber())
                .driverUsername(car.getDriver().getUsername())
                .driverEmail(car.getDriver().getEmail())
                .build();
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    @Override
    public CarDTO addCar(CarDTO dto, String email) {
        User driver = getUserByEmail(email);

        Car car = new Car();
        car.setModel(dto.getModel());
        car.setSeats(dto.getSeats());
        car.setAvailableSeats(dto.getSeats()); // Au depart, tous les sieges sont disponibles
        car.setPlateNumber(dto.getPlateNumber());
        car.setDriver(driver);

        return toDTO(carRepository.save(car));
    }

    @Override
    public List<CarDTO> getAllCars() {
        return carRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CarDTO> getMyCars(String email) {
        User driver = getUserByEmail(email);
        return carRepository.findByDriverId(driver.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CarDTO getCarById(Long carId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found: " + carId));
        return toDTO(car);
    }

    @Override
    public void deleteCar(Long carId, String email) {
        User driver = getUserByEmail(email);
        Car car = carRepository.findByIdAndDriverId(carId, driver.getId())
                .orElseThrow(() -> new RuntimeException("Car not found or access denied"));
        carRepository.delete(car);
    }
}