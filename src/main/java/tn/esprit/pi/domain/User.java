package tn.esprit.pi.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Post> posts = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Like> likes = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CommentReaction> commentReactions = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
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
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Order> orders;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductReview> productReviews;

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
    public void addPost(Post post) {
        post.setUser(this);   // FK user_id dans post
        this.posts.add(post);
    }

    public void removePost(Post post) {
        post.setUser(null);
        this.posts.remove(post);
    }
    public void addComment(Comment comment) {
        comment.setUser(this);  // FK user_id dans comment
        this.comments.add(comment);
    }

    public void removeComment(Comment comment) {
        comment.setUser(null);
        this.comments.remove(comment);
    }
    public void addLike(Like like) {
        like.setUser(this);   // FK user_id dans like
        this.likes.add(like);
    }

    public void removeLike(Like like) {
        like.setUser(null);
        this.likes.remove(like);
    }
    public void addCommentReaction(CommentReaction reaction) {
        reaction.setUser(this);  // FK user_id dans comment_reaction
        this.commentReactions.add(reaction);
    }

    public void removeCommentReaction(CommentReaction reaction) {
        reaction.setUser(null);
        this.commentReactions.remove(reaction);
    }
}
