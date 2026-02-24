package tn.esprit.pi.dto;

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
    private String route;
    private LocalDate date;
    private LocalTime departureTime;
    private Long carId;
    private String carModel;
    private String plateNumber;
    private Integer availableSeats;
    private String driverUsername;
    private String driverEmail;
    private List<String> participantUsernames;
    private Integer participantCount;
}