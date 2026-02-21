package tn.esprit.pi.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Player extends AppUser {
    private LocalDate dateOfBirth;
    
    @Enumerated(EnumType.STRING)
    private PlayerLevel level;
    
    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
    private MedicalRecord medicalRecord;
    
    @ManyToMany(mappedBy = "players")
    private Set<Team> teams;
    
    @ManyToMany(mappedBy = "participants")
    private Set<Training> trainings;
}
