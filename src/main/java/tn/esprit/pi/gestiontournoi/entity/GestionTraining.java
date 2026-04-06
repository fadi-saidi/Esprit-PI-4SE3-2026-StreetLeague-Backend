package tn.esprit.pi.gestiontournoi.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "gestion_trainings")
@Getter
@Setter
@NoArgsConstructor
public class GestionTraining extends GestionApprovalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    private Long coachUserId;

    @NotBlank
    private String coachName;

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

    @ElementCollection
    @CollectionTable(name = "gestion_training_players", joinColumns = @JoinColumn(name = "training_id"))
    @Column(name = "user_id")
    private Set<Long> selectedPlayerUserIds = new LinkedHashSet<>();
}
