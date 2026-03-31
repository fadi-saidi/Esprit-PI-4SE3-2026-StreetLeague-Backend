package tn.esprit.pi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.domain.Car;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.dto.CarDTO;
import tn.esprit.pi.repository.CarRepository;
import tn.esprit.pi.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceImplTest {

    @Mock private CarRepository  carRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private CarServiceImpl carService;

    private User driver;
    private Car  car;

    @BeforeEach
    void setUp() {
        driver = new User();
        driver.setId(1L);
        driver.setUsername("alice");
        driver.setEmail("alice@test.com");

        car = new Car();
        car.setId(100L);
        car.setModel("Toyota Corolla");
        car.setSeats(5);
        car.setAvailableSeats(5);
        car.setPlateNumber("123TUN456");
        car.setDriver(driver);
    }

    // ── addCar ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addCar: creates car with all seats available initially")
    void addCar_success() {
        CarDTO dto = CarDTO.builder()
                .model("Toyota Corolla").seats(5).plateNumber("123TUN456").build();

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(driver));
        when(carRepository.save(any(Car.class))).thenReturn(car);

        CarDTO result = carService.addCar(dto, "alice@test.com");

        assertThat(result.getModel()).isEqualTo("Toyota Corolla");
        assertThat(result.getSeats()).isEqualTo(5);
        assertThat(result.getAvailableSeats()).isEqualTo(5);
        assertThat(result.getDriverEmail()).isEqualTo("alice@test.com");
        verify(carRepository).save(any(Car.class));
    }

    @Test
    @DisplayName("addCar: unknown user throws RuntimeException")
    void addCar_unknownUser() {
        CarDTO dto = CarDTO.builder().model("X").seats(2).plateNumber("AB123").build();
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.addCar(dto, "ghost@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    // ── getMyCars ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getMyCars: returns cars belonging to driver")
    void getMyCars_success() {
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(driver));
        when(carRepository.findByDriverId(1L)).thenReturn(List.of(car));

        List<CarDTO> result = carService.getMyCars("alice@test.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlateNumber()).isEqualTo("123TUN456");
    }

    @Test
    @DisplayName("getMyCars: driver with no cars returns empty list")
    void getMyCars_empty() {
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(driver));
        when(carRepository.findByDriverId(1L)).thenReturn(List.of());

        List<CarDTO> result = carService.getMyCars("alice@test.com");

        assertThat(result).isEmpty();
    }

    // ── getAllCars ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllCars: returns all cars")
    void getAllCars_success() {
        when(carRepository.findAll()).thenReturn(List.of(car));

        List<CarDTO> result = carService.getAllCars();

        assertThat(result).hasSize(1);
    }

    // ── getCarById ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getCarById: existing id returns DTO")
    void getCarById_found() {
        when(carRepository.findById(100L)).thenReturn(Optional.of(car));

        CarDTO result = carService.getCarById(100L);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getModel()).isEqualTo("Toyota Corolla");
    }

    @Test
    @DisplayName("getCarById: unknown id throws RuntimeException")
    void getCarById_notFound() {
        when(carRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.getCarById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Car not found");
    }

    // ── deleteCar ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteCar: deletes car owned by driver")
    void deleteCar_success() {
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(driver));
        when(carRepository.findByIdAndDriverId(100L, 1L)).thenReturn(Optional.of(car));

        carService.deleteCar(100L, "alice@test.com");

        verify(carRepository).delete(car);
    }

    @Test
    @DisplayName("deleteCar: car not owned by user throws RuntimeException")
    void deleteCar_accessDenied() {
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(driver));
        when(carRepository.findByIdAndDriverId(100L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.deleteCar(100L, "alice@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("access denied");
    }

    // ── updatePhotoUrl ────────────────────────────────────────────────────────

    @Test
    @DisplayName("updatePhotoUrl: saves new photoUrl")
    void updatePhotoUrl_success() {
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(driver));
        when(carRepository.findByIdAndDriverId(100L, 1L)).thenReturn(Optional.of(car));
        when(carRepository.save(any())).thenReturn(car);

        CarDTO result = carService.updatePhotoUrl(100L, "/photos/car.jpg", "alice@test.com");

        assertThat(car.getPhotoUrl()).isEqualTo("/photos/car.jpg");
        verify(carRepository).save(car);
    }
}
