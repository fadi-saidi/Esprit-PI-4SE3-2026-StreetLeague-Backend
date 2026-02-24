package tn.esprit.pi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.Car;
import tn.esprit.pi.domain.Carpooling;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.dto.*;
import tn.esprit.pi.repository.CarRepository;
import tn.esprit.pi.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCarpoolingServiceImpl implements IAdminCarpoolingService {

    private final UserRepository userRepository;
    private final CarRepository carRepository;

    // ─── Mappers ──────────────────────────────────────────────────────────────

    private ParticipantDTO toParticipantDTO(User user) {
        return ParticipantDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }

    private CarpoolingWithParticipantsDTO toCarpoolingDTO(Carpooling c) {
        List<ParticipantDTO> participants = c.getParticipants() != null
                ? c.getParticipants().stream()
                .map(this::toParticipantDTO)
                .collect(Collectors.toList())
                : List.of();

        return CarpoolingWithParticipantsDTO.builder()
                .carpoolingId(c.getId())
                .route(c.getRoute())
                .date(c.getDate())
                .departureTime(c.getDepartureTime())
                .participantCount(participants.size())
                .participants(participants)
                .build();
    }

    private CarWithCarpoolingsDTO toCarWithCarpoolingsDTO(Car car) {
        List<CarpoolingWithParticipantsDTO> carpoolings = car.getCarpoolings() != null
                ? car.getCarpoolings().stream()
                .map(this::toCarpoolingDTO)
                .collect(Collectors.toList())
                : List.of();

        return CarWithCarpoolingsDTO.builder()
                .carId(car.getId())
                .model(car.getModel())
                .seats(car.getSeats())
                .availableSeats(car.getAvailableSeats())
                .plateNumber(car.getPlateNumber())
                .carpoolings(carpoolings)
                .build();
    }

    private DriverWithCarsAndCarpoolingsDTO toDriverDTO(User driver) {
        List<CarWithCarpoolingsDTO> cars = driver.getCars() != null
                ? driver.getCars().stream()
                .map(this::toCarWithCarpoolingsDTO)
                .collect(Collectors.toList())
                : List.of();

        return DriverWithCarsAndCarpoolingsDTO.builder()
                .driverId(driver.getId())
                .driverUsername(driver.getUsername())
                .driverEmail(driver.getEmail())
                .driverPhone(driver.getPhone())
                .cars(cars)
                .build();
    }

    // ─── Service methods ──────────────────────────────────────────────────────

    @Override
    public List<DriverWithCarsAndCarpoolingsDTO> getAllDriversWithCarsAndCarpoolings() {
        // Recuperer uniquement les users qui ont au moins une voiture
        return carRepository.findAll()
                .stream()
                .map(Car::getDriver)
                .distinct()
                .map(this::toDriverDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DriverWithCarsAndCarpoolingsDTO getDriverWithCarsAndCarpoolings(Long driverId) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + driverId));
        return toDriverDTO(driver);
    }
}