package tn.esprit.pi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@DiscriminatorValue("MATCH")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Match extends Evenement {

    @NotBlank
    @Column(nullable = false)
    private String equipeA;

    @NotBlank
    @Column(nullable = false)
    private String equipeB;

    @Min(0)
    @NotNull
    @Column(nullable = false)
    private Integer scoreA;

    @Min(0)
    @NotNull
    @Column(nullable = false)
    private Integer scoreB;
}