package tn.esprit.pi.dto;

import lombok.*;
import tn.esprit.pi.domain.SportType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueDTO {
    private Long id;
    private String name;
    private String address;
    private Double pricePerHour;
    private Integer capacity;
    private SportType sportType;
    private String photoUrl;
    private Boolean available;
}