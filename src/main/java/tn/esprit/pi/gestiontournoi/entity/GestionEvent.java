package tn.esprit.pi.gestiontournoi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gestion_events")
@Getter
@Setter
@NoArgsConstructor
public class GestionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank
    @Column(nullable = false)
    private String date;

    @NotBlank
    @Column(nullable = false)
    private String location;
}
