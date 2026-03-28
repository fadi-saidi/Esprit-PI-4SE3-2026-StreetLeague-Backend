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
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Wallet wallet;
    
    @ManyToMany
    @JoinTable(name = "user_badge",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "badge_id"))
    private Set<Badge> badges;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Post> posts = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Like> likes = new HashSet<>();

    // ✅ NOUVEAU — réactions sur les comments
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CommentReaction> commentReactions = new HashSet<>();
    
    @OneToMany(mappedBy = "user")
    private Set<Cart> carts;
    
    @OneToMany(mappedBy = "driver")
    private Set<Car> cars;
    
    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private Set<VirtualTeam> virtualTeams;
    
    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private Set<OwnedPlayer> ownedPlayers;
    
    @OneToMany(mappedBy = "user")
    private Set<Reward> rewards;
    
    @OneToMany(mappedBy = "user")
    private Set<Reservation> reservations;
    
    // Profile relationships
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private CoachProfile coachProfile;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private PlayerProfile playerProfile;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private RefereeProfile refereeProfile;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private HealthProfessionalProfile healthProfessionalProfile;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private SponsorProfile sponsorProfile;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private VenueOwnerProfile venueOwnerProfile;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
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
