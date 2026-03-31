package tn.esprit.pi.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Admin (or future API) fills this after each week.
 * The scheduler reads from here to resolve predictions.
 */
@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PlayerStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long playerId;
    private String playerName;

    @Enumerated(EnumType.STRING)
    private SportType sportType;

    private int weekNumber;
    private int weekYear;

    // FOOTBALL
    private int goalsScored;
    private int yellowCards;
    private int redCards;

    // BASKETBALL
    private int basketballPoints;

    // TENNIS
    private boolean tennisWin;
}