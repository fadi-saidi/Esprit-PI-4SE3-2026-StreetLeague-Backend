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
public class CarpoolingWithParticipantsDTO {

    // Infos du trajet
    private Long carpoolingId;
    private String route;
    private LocalDate date;
    private LocalTime departureTime;
    private Integer participantCount;

    // Les membres participants
    private List<ParticipantDTO> participants;
}