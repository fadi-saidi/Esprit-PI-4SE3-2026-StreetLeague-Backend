package tn.esprit.pi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.pi.domain.ReservationStatus;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReservationDTO {

    private Long id;

    @NotNull(message = "Venue is required")
    private Long venueId;

    private String venueName;
    private String venueAddress;

    private Long   userId;
    private String userName;

    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Date must be today or in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;

    @NotNull(message = "Duration is required")
    @Min(value = 1,  message = "Duration must be at least 1 hour")
    @Max(value = 12, message = "Duration cannot exceed 12 hours")
    private Integer duration;

    private Double price;

    private ReservationStatus status;

    // Optional: reason text used for BLOCKED reservations
    private String reason;
}
