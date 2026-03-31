package tn.esprit.pi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.domain.Car;
import tn.esprit.pi.domain.Carpooling;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.dto.CarpoolingDTO;
import tn.esprit.pi.repository.CarRepository;
import tn.esprit.pi.repository.CarpoolingRepository;
import tn.esprit.pi.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarpoolingServiceImplTest {

    @Mock private CarpoolingRepository carpoolingRepository;
    @Mock private CarRepository        carRepository;
    @Mock private UserRepository       userRepository;

    @InjectMocks private CarpoolingServiceImpl carpoolingService;

    private User      driver;
    private Car       car;
    private Carpooling carpooling;

    @BeforeEach
    void setUp() {
        driver = new User();
        driver.setId(1L);
        driver.setUsername("alice");
        driver.setEmail("alice@test.com");

        car = new Car();
        car.setId(10L);
        car.setModel("Toyota");
        car.setSeats(4);
        car.setAvailableSeats(3);
        car.setPlateNumber("123TUN");
        car.setDriver(driver);

        carpooling = new Carpooling();
        carpooling.setId(100L);
        carpooling.setDepartureLocation("Tunis");
        carpooling.setArrivalLocation("Sousse");
        carpooling.setDate(LocalDate.now().plusDays(1));
        carpooling.setDepartureTime(LocalTime.of(8, 0));
        carpooling.setCar(car);
        carpooling.setParticipants(new HashSet<>());
    }

    // ── createCarpooling ──────────────────────────────────────────────────────

    @Test
    @DisplayName("createCarpooling: saves trip with correct data")
    void createCarpooling_success() {
        CarpoolingDTO dto = CarpoolingDTO.builder()
                .departureLocation("Tunis")
                .arrivalLocation("Sousse")
                .date(LocalDate.now().plusDays(1))
                .departureTime(LocalTime.of(8, 0))
                .carId(10L)
                .build();

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(driver));
        when(carRepository.findByIdAndDriverId(10L, 1L)).thenReturn(Optional.of(car));
        when(carpoolingRepository.save(any())).thenReturn(carpooling);

        CarpoolingDTO result = carpoolingService.createCarpooling(dto, "alice@test.com");

        assertThat(result.getDepartureLocation()).isEqualTo("Tunis");
        assertThat(result.getArrivalLocation()).isEqualTo("Sousse");
        verify(carpoolingRepository).save(any(Carpooling.class));
    }

    @Test
    @DisplayName("createCarpooling: car not owned by driver throws RuntimeException")
    void createCarpooling_carNotOwned() {
        CarpoolingDTO dto = CarpoolingDTO.builder()
                .departureLocation("Tunis").arrivalLocation("Sfax")
                .date(LocalDate.now().plusDays(1)).departureTime(LocalTime.of(9, 0))
                .carId(99L).build();

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(driver));
        when(carRepository.findByIdAndDriverId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carpoolingService.createCarpooling(dto, "alice@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Car not found");
    }

    // ── getAllCarpoolings ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllCarpoolings: returns all trips as DTOs")
    void getAllCarpoolings_success() {
        when(carpoolingRepository.findAll()).thenReturn(List.of(carpooling));

        List<CarpoolingDTO> result = carpoolingService.getAllCarpoolings();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDepartureLocation()).isEqualTo("Tunis");
    }

    // ── getMyCreatedCarpoolings ────────────────────────────────────────────────

    @Test
    @DisplayName("getMyCreatedCarpoolings: returns trips created by driver")
    void getMyCreatedCarpoolings_success() {
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(driver));
        when(carpoolingRepository.findByCar_DriverId(1L)).thenReturn(List.of(carpooling));

        List<CarpoolingDTO> result = carpoolingService.getMyCreatedCarpoolings("alice@test.com");

        assertThat(result).hasSize(1);
    }

    // ── joinCarpooling ────────────────────────────────────────────────────────

    @Test
    @DisplayName("joinCarpooling: participant added and seats decremented")
    void joinCarpooling_success() {
        User passenger = new User();
        passenger.setId(2L);
        passenger.setUsername("bob");
        passenger.setEmail("bob@test.com");

        when(userRepository.findByEmail("bob@test.com")).thenReturn(Optional.of(passenger));
        when(carpoolingRepository.findById(100L)).thenReturn(Optional.of(carpooling));
        when(carpoolingRepository.save(any())).thenReturn(carpooling);

        CarpoolingDTO result = carpoolingService.joinCarpooling(100L, "bob@test.com");

        assertThat(carpooling.getParticipants()).contains(passenger);
        assertThat(car.getAvailableSeats()).isEqualTo(2);
    }

    @Test
    @DisplayName("joinCarpooling: driver cannot join own trip")
    void joinCarpooling_driverJoinsSelf() {
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(driver));
        when(carpoolingRepository.findById(100L)).thenReturn(Optional.of(carpooling));

        assertThatThrownBy(() -> carpoolingService.joinCarpooling(100L, "alice@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("driver");
    }

    @Test
    @DisplayName("joinCarpooling: no available seats throws RuntimeException")
    void joinCarpooling_noSeats() {
        car.setAvailableSeats(0);
        User passenger = new User(); passenger.setId(2L); passenger.setEmail("bob@test.com");

        when(userRepository.findByEmail("bob@test.com")).thenReturn(Optional.of(passenger));
        when(carpoolingRepository.findById(100L)).thenReturn(Optional.of(carpooling));

        assertThatThrownBy(() -> carpoolingService.joinCarpooling(100L, "bob@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No available seats");
    }

    @Test
    @DisplayName("joinCarpooling: already participating throws RuntimeException")
    void joinCarpooling_alreadyJoined() {
        User passenger = new User(); passenger.setId(2L); passenger.setEmail("bob@test.com");
        carpooling.getParticipants().add(passenger);

        when(userRepository.findByEmail("bob@test.com")).thenReturn(Optional.of(passenger));
        when(carpoolingRepository.findById(100L)).thenReturn(Optional.of(carpooling));

        assertThatThrownBy(() -> carpoolingService.joinCarpooling(100L, "bob@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already");
    }

    // ── leaveCarpooling ───────────────────────────────────────────────────────

    @Test
    @DisplayName("leaveCarpooling: removes participant and frees seat")
    void leaveCarpooling_success() {
        User passenger = new User(); passenger.setId(2L); passenger.setEmail("bob@test.com");
        carpooling.getParticipants().add(passenger);
        car.setAvailableSeats(2);

        when(userRepository.findByEmail("bob@test.com")).thenReturn(Optional.of(passenger));
        when(carpoolingRepository.findById(100L)).thenReturn(Optional.of(carpooling));
        when(carpoolingRepository.save(any())).thenReturn(carpooling);

        carpoolingService.leaveCarpooling(100L, "bob@test.com");

        assertThat(carpooling.getParticipants()).doesNotContain(passenger);
        assertThat(car.getAvailableSeats()).isEqualTo(3);
    }

    // ── deleteCarpooling ──────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteCarpooling: driver can delete own trip")
    void deleteCarpooling_success() {
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(driver));
        when(carpoolingRepository.findById(100L)).thenReturn(Optional.of(carpooling));

        carpoolingService.deleteCarpooling(100L, "alice@test.com");

        verify(carpoolingRepository).delete(carpooling);
    }

    @Test
    @DisplayName("deleteCarpooling: non-driver throws RuntimeException")
    void deleteCarpooling_notDriver() {
        User other = new User(); other.setId(9L); other.setEmail("other@test.com");

        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(other));
        when(carpoolingRepository.findById(100L)).thenReturn(Optional.of(carpooling));

        assertThatThrownBy(() -> carpoolingService.deleteCarpooling(100L, "other@test.com"))
                .isInstanceOf(RuntimeException.class);
    }
}
