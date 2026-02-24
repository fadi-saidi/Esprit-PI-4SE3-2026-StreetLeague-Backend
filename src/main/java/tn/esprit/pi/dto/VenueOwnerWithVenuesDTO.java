package tn.esprit.pi.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueOwnerWithVenuesDTO {

    // Owner info
    private Long ownerId;
    private String ownerEmail;
    private String ownerUsername;
    private String ownerPhone;
    private String companyName;
    private Boolean verified;

    // Ses venues en dessous
    private List<VenueDTO> venues;
}