package tn.esprit.pi.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Coach extends AppUser {
    private String certificate;
    private Integer experienceYears;
    private String specialty;
    private Boolean verified;
    
    @OneToMany(mappedBy = "coach")
    private Set<Team> teams;
    
    @OneToMany(mappedBy = "coach")
    private Set<Training> trainings;
}
