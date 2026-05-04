package tn.esprit.pi.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    private LocalDateTime creationDate;

    private boolean flagged = false;
    private String flagReason;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({
            "adminProfile",
            "coachProfile",
            "playerProfile",
            "refereeProfile",
            "sponsorProfile",
            "venueOwnerProfile",
            "wallet",
            "medicalRecord"
    })
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("post")
    private Set<Like> likes = new HashSet<>();
    @PrePersist
    public void onCreate() {
        this.creationDate = LocalDateTime.now();

    }
    public void addLike(Like like) {
        like.setPost(this);
        this.likes.add(like);
    }

    public void removeLike(Like like) {
        like.setPost(null);
        this.likes.remove(like);
    }

    // ✅ PARENT gère l'affectation du Comment
    public void addComment(Comment comment) {
        comment.setPost(this);
        this.comments.add(comment);
    }

    public void removeComment(Comment comment) {
        comment.setPost(null);
        this.comments.remove(comment);
    }
}

