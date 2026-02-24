package tn.esprit.pi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tournois")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tournoi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nom;

    @NotNull
    @Column(nullable = false)
    private LocalDate dateDebut;

    @NotNull
    @Column(nullable = false)
    private LocalDate dateFin;

    @NotBlank
    @Column(nullable = false)
    private String lieu;

    @OneToMany(mappedBy = "tournoi", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Evenement> evenements = new ArrayList<>();
}