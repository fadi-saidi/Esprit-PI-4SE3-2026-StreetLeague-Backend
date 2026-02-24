package tn.esprit.pi.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverWithCarsAndCarpoolingsDTO {

    // Infos du driver
    private Long driverId;
    private String driverUsername;
    private String driverEmail;
    private String driverPhone;

    // Ses voitures avec leurs carpoolings en dessous
    private List<CarWithCarpoolingsDTO> cars;
}