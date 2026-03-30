package tn.esprit.pi.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String phone;
    private String email;
    private String password;
    
    @Enumerated(EnumType.STRING)
    private Role role;
    
    private Boolean enabled;
    private String address;
    private Integer rank;
    private LocalDateTime createdAt;
    
    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Wallet wallet;
    
    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "user_badge",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "badge_id"))
    private Set<Badge> badges;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Set<Post> posts;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Set<Comment> comments;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Set<Like> likes;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Set<Cart> carts;

    @JsonIgnore
    @OneToMany(mappedBy = "driver")
    private Set<Car> cars;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Set<VirtualTeam> virtualTeams;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Set<OwnedPlayer> ownedPlayers;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Set<Reward> rewards;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Set<Reservation> reservations;
    
    // Profile relationships
    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CoachProfile coachProfile;

    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PlayerProfile playerProfile;

    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private RefereeProfile refereeProfile;

    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private HealthProfessionalProfile healthProfessionalProfile;

    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private SponsorProfile sponsorProfile;

    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private VenueOwnerProfile venueOwnerProfile;

    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AdminProfile adminProfile;
}
