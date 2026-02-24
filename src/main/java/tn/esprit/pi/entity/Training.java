package tn.esprit.pi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@DiscriminatorValue("TRAINING")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Training extends Evenement {

    @NotBlank
    @Column(nullable = false)
    private String coach;

    @Min(1)
    @Column(nullable = false)
    private Integer dureeMinutes;

    private String objectif;
}
