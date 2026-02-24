package tn.esprit.pi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.Car;
import tn.esprit.pi.domain.Carpooling;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.dto.CarpoolingDTO;
import tn.esprit.pi.repository.CarRepository;
import tn.esprit.pi.repository.CarpoolingRepository;
import tn.esprit.pi.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarpoolingServiceImpl implements ICarpoolingService {

    private final CarpoolingRepository carpoolingRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    private CarpoolingDTO toDTO(Carpooling c) {
        List<String> participants = c.getParticipants() != null
                ? c.getParticipants().stream()
                .map(User::getUsername)
                .collect(Collectors.toList())
                : List.of();

        return CarpoolingDTO.builder()
                .id(c.getId())
                .route(c.getRoute())
                .date(c.getDate())
                .departureTime(c.getDepartureTime())
                .carId(c.getCar().getId())
                .carModel(c.getCar().getModel())
                .plateNumber(c.getCar().getPlateNumber())
                .availableSeats(c.getCar().getAvailableSeats())
                .driverUsername(c.getCar().getDriver().getUsername())
                .driverEmail(c.getCar().getDriver().getEmail())
                .participantUsernames(participants)
                .participantCount(participants.size())
                .build();
    }

    // ─── Operations ───────────────────────────────────────────────────────────

    @Override
    public CarpoolingDTO createCarpooling(CarpoolingDTO dto, String email) {
        User driver = getUserByEmail(email);

        // Verifier que la voiture appartient au driver connecte
        Car car = carRepository.findByIdAndDriverId(dto.getCarId(), driver.getId())
                .orElseThrow(() -> new RuntimeException("Car not found or does not belong to you"));

        Carpooling carpooling = new Carpooling();
        carpooling.setRoute(dto.getRoute());
        carpooling.setDate(dto.getDate());
        carpooling.setDepartureTime(dto.getDepartureTime());
        carpooling.setCar(car);
        carpooling.setParticipants(new HashSet<>());

        return toDTO(carpoolingRepository.save(carpooling));
    }

    @Override
    public List<CarpoolingDTO> getAllCarpoolings() {
        return carpoolingRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CarpoolingDTO> getMyCreatedCarpoolings(String email) {
        User driver = getUserByEmail(email);
        return carpoolingRepository.findByCar_DriverId(driver.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CarpoolingDTO> getMyJoinedCarpoolings(String email) {
        User user = getUserByEmail(email);
        return carpoolingRepository.findByParticipants_Id(user.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CarpoolingDTO getCarpoolingById(Long carpoolingId) {
        Carpooling c = carpoolingRepository.findById(carpoolingId)
                .orElseThrow(() -> new RuntimeException("Carpooling not found: " + carpoolingId));
        return toDTO(c);
    }

    @Override
    public CarpoolingDTO joinCarpooling(Long carpoolingId, String email) {
        User user = getUserByEmail(email);
        Carpooling carpooling = carpoolingRepository.findById(carpoolingId)
                .orElseThrow(() -> new RuntimeException("Carpooling not found: " + carpoolingId));

        // Verifier que ce n'est pas le driver lui-meme
        if (carpooling.getCar().getDriver().getId().equals(user.getId())) {
            throw new RuntimeException("You are the driver, you cannot join your own carpooling");
        }

        // Verifier qu'il reste des places
        Car car = carpooling.getCar();
        if (car.getAvailableSeats() <= 0) {
            throw new RuntimeException("No available seats");
        }

        // Verifier que le user n'est pas deja participant
        if (carpooling.getParticipants().stream().anyMatch(p -> p.getId().equals(user.getId()))) {
            throw new RuntimeException("You already joined this carpooling");
        }

        // Ajouter le participant et decrementer les places
        carpooling.getParticipants().add(user);
        car.setAvailableSeats(car.getAvailableSeats() - 1);
        carRepository.save(car);

        return toDTO(carpoolingRepository.save(carpooling));
    }

    @Override
    public CarpoolingDTO leaveCarpooling(Long carpoolingId, String email) {
        User user = getUserByEmail(email);
        Carpooling carpooling = carpoolingRepository.findById(carpoolingId)
                .orElseThrow(() -> new RuntimeException("Carpooling not found: " + carpoolingId));

        // Verifier que le user est bien participant
        boolean isParticipant = carpooling.getParticipants()
                .stream().anyMatch(p -> p.getId().equals(user.getId()));
        if (!isParticipant) {
            throw new RuntimeException("You are not a participant of this carpooling");
        }

        // Retirer le participant et incrementer les places
        carpooling.getParticipants().removeIf(p -> p.getId().equals(user.getId()));
        Car car = carpooling.getCar();
        car.setAvailableSeats(car.getAvailableSeats() + 1);
        carRepository.save(car);

        return toDTO(carpoolingRepository.save(carpooling));
    }

    @Override
    public void deleteCarpooling(Long carpoolingId, String email) {
        User driver = getUserByEmail(email);
        Carpooling carpooling = carpoolingRepository.findById(carpoolingId)
                .orElseThrow(() -> new RuntimeException("Carpooling not found: " + carpoolingId));

        // Verifier que c'est bien le driver qui supprime
        if (!carpooling.getCar().getDriver().getId().equals(driver.getId())) {
            throw new RuntimeException("Access denied: you are not the driver of this carpooling");
        }

        // Remettre les places disponibles
        Car car = carpooling.getCar();
        car.setAvailableSeats(car.getAvailableSeats() + carpooling.getParticipants().size());
        carRepository.save(car);

        carpoolingRepository.delete(carpooling);
    }
}