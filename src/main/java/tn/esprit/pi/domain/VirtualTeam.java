package tn.esprit.pi.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VirtualTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private SportType sportType;

    private Double earnedPoints;
    private Double weekPoints;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ElementCollection
    @CollectionTable(name = "virtual_team_player_ids", joinColumns = @JoinColumn(name = "virtual_team_id"))
    @Column(name = "player_id")
    private List<Long> playerIds = new ArrayList<>();
}
