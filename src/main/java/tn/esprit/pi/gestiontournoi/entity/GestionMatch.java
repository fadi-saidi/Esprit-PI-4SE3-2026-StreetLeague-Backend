package tn.esprit.pi.gestiontournoi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gestion_matches")
@Getter
@Setter
@NoArgsConstructor
public class GestionMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String homeTeam;

    @NotBlank
    @Column(nullable = false)
    private String awayTeam;

    @NotBlank
    @Column(nullable = false)
    private String date;

    private String score;

    @NotBlank
    @Column(nullable = false)
    private String location;
}
