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
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "gestion_events")
@Getter
@Setter
@NoArgsConstructor
public class GestionEvent extends GestionApprovalEntity {

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

    @ElementCollection
    @CollectionTable(name = "gestion_event_participants", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "user_id")
    private Set<Long> participantUserIds = new LinkedHashSet<>();
}
