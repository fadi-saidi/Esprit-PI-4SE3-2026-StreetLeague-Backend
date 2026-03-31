package tn.esprit.pi.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "virtual_team_id", nullable = false)
    private VirtualTeam virtualTeam;

    private int weekNumber;   // ISO week number (1–52)
    private int weekYear;     // year of the week

    // Captain player ID (points × 2)
    private Long captainPlayerId;

    @Enumerated(EnumType.STRING)
    private PredictionStatus status; // PENDING, RESOLVED

    private Double totalPointsEarned;

    private LocalDate createdAt;

    @OneToMany(mappedBy = "prediction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerPrediction> playerPredictions = new ArrayList<>();
}