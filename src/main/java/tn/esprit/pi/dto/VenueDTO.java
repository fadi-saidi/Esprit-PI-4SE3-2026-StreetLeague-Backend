package tn.esprit.pi.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.pi.domain.SportType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueDTO {
    private Long id;

    @NotBlank(message = "Venue name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address is too long")
    private String address;

    @NotNull(message = "Price per hour is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "10000.0", message = "Price cannot exceed 10000")
    private Double pricePerHour;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 500, message = "Capacity cannot exceed 500")
    private Integer capacity;

    @NotNull(message = "Sport type is required")
    private SportType sportType;

    private String photoUrl;
    private Boolean available;
    private Boolean verified;
    private String ownerName;
}