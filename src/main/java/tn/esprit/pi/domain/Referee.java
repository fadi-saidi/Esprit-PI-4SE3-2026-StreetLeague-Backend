package tn.esprit.pi.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Referee extends AppUser {
    private String certificate;
    private Integer experienceYears;
    private String licenseNumber;
    private Boolean verified;
    
    @OneToMany(mappedBy = "referee")
    private Set<Match> matches;
}
