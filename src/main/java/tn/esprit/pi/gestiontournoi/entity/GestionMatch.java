package tn.esprit.pi.gestiontournoi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gestion_matches")
@Getter
@Setter
@NoArgsConstructor
public class GestionMatch extends GestionApprovalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long homeTeamId;

    private String homeTeamName;

    private Long awayTeamId;

    private String awayTeamName;

    private Long refereeUserId;

    private String refereeName;

    @NotBlank
    @Column(nullable = false)
    private String date;

    private String score;

    @NotBlank
    @Column(nullable = false)
    private String location;
}
