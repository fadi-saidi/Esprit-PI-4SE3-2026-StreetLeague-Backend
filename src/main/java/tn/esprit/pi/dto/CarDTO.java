package tn.esprit.pi.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarDTO {
    private Long id;
    private String model;
    private Integer seats;
    private Integer availableSeats;
    private String plateNumber;
    private String photoUrl;
    private String driverUsername;
    private String driverEmail;
}