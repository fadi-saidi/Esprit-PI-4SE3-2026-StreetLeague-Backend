package tn.esprit.pi.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarpoolingDTO {
    private Long id;

    @NotBlank(message = "Departure location is required")
    @Size(max = 255, message = "Departure location is too long")
    private String departureLocation;

    @NotBlank(message = "Arrival location is required")
    @Size(max = 255, message = "Arrival location is too long")
    private String arrivalLocation;

    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Date must be today or in the future")
    private LocalDate date;

    @NotNull(message = "Departure time is required")
    private LocalTime departureTime;

    @NotNull(message = "Car is required")
    private Long carId;
    private String carModel;
    private String plateNumber;
    private String carPhotoUrl;
    private Integer availableSeats;
    private String driverUsername;
    private String driverEmail;
    private List<String> participantUsernames;
    private List<String> participantEmails;
    private Integer participantCount;
}