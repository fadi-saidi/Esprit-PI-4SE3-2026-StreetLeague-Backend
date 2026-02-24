package tn.esprit.pi.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Date et heure de la réservation
    private LocalDateTime date;

    // Durée en minutes
    private Integer duration;

    // Prix total
    private Double price;

    // Statut de la réservation
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    // Lieu réservé
    @ManyToOne
    @JoinColumn(name = "venue_id")
    @JsonIgnoreProperties({"reservations"})
    private Venue venue;

    // Utilisateur ayant fait la réservation
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"reservations"})
    private User user;


}