package tn.esprit.pi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import tn.esprit.pi.domain.ReservationStatus;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReservationDTO {
    private Long id;
    private Long venueId;
    private String venueName;
    private String venueAddress;
    private Long userId;
    private String userName;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;
    private Integer duration;
    private Double price;
    private ReservationStatus status;
}
