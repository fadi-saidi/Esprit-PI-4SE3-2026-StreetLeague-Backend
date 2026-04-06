package tn.esprit.pi.gestiontournoi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gestion_tournaments")
@Getter
@Setter
@NoArgsConstructor
public class GestionTournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String startDate;

    @NotBlank
    @Column(nullable = false)
    private String endDate;

    @NotBlank
    @Column(nullable = false)
    private String location;

    @NotNull
    @Min(2)
    @Column(nullable = false)
    private Integer maxTeams;
}
