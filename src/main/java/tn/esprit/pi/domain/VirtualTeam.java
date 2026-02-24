package tn.esprit.pi.domain;

import jakarta.persistence.*;
import lombok.*;

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

    @Enumerated(EnumType.STRING)
    private SportType sportType;

    private Double earnedPoints;
    private Double weekPoints;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "virtualTeam", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OwnedPlayer> ownedPlayers;
}