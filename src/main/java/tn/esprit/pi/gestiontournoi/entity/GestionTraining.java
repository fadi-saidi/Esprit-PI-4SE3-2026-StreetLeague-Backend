package tn.esprit.pi.gestiontournoi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gestion_trainings")
@Getter
@Setter
@NoArgsConstructor
public class GestionTraining {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(nullable = false)
    private String coach;

    @NotBlank
    @Column(nullable = false)
    private String date;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer duration;

    @NotBlank
    @Column(nullable = false)
    private String location;
}
