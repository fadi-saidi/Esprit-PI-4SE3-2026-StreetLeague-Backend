package tn.esprit.pi.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthProfessional extends AppUser {
    private String certificate;
    private String specialty;
    private String licenseNumber;
    private Boolean verified;
    
    @OneToMany(mappedBy = "healthProfessional")
    private Set<MedicalRecord> medicalRecords;
}
