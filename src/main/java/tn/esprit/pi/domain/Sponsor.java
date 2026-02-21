package tn.esprit.pi.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sponsor extends AppUser {
    private String companyName;
    private String logo;
    private String contactEmail;
    
    @OneToMany(mappedBy = "sponsor")
    private Set<Sponsorship> sponsorships;
}
