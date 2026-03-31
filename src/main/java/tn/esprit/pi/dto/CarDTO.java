package tn.esprit.pi.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarDTO {
    private Long id;

    @NotBlank(message = "Model is required")
    @Size(min = 2, max = 60, message = "Model must be between 2 and 60 characters")
    private String model;

    @NotNull(message = "Total seats is required")
    @Min(value = 1, message = "Seats must be at least 1")
    @Max(value = 20, message = "Seats cannot exceed 20")
    private Integer seats;

    @NotNull(message = "Available seats is required")
    @Min(value = 0, message = "Available seats cannot be negative")
    private Integer availableSeats;

    @NotBlank(message = "Plate number is required")
    @Pattern(regexp = "^[A-Za-z0-9\\s\\-]{3,20}$", message = "Invalid plate number format")
    private String plateNumber;

    private String photoUrl;
    private String driverUsername;
    private String driverEmail;
}