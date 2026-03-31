package tn.esprit.pi.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarWithCarpoolingsDTO {

    // Infos de la voiture
    private Long carId;
    private String model;
    private Integer seats;
    private Integer availableSeats;
    private String plateNumber;

    // Les carpoolings de cette voiture
    private List<CarpoolingWithParticipantsDTO> carpoolings;
}