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
@Table(name = "gestion_tournaments")
@Getter
@Setter
@NoArgsConstructor
public class GestionTournament extends GestionApprovalEntity {

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

    @ElementCollection
    @CollectionTable(name = "gestion_tournament_participants", joinColumns = @JoinColumn(name = "tournament_id"))
    @Column(name = "user_id")
    private Set<Long> participantUserIds = new LinkedHashSet<>();
}
