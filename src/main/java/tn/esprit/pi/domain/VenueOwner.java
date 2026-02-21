package tn.esprit.pi.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VenueOwner extends AppUser {
    private String companyName;
    private String phone;
    private Boolean verified;
    
    @OneToMany(mappedBy = "venueOwner")
    private Set<Venue> venues;
}
