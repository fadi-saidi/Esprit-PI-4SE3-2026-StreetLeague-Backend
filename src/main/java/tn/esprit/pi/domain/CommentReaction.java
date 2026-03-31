package tn.esprit.pi.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comment_reaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CommentReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // ✅ même enum que Like — LIKE, LOVE, HAHA, WOW, SAD, ANGRY
    @Enumerated(EnumType.STRING)
    private LikeType likeType;

    private LocalDateTime creationDate;

    // ✅ réaction appartient à un Comment (parent)
    @ManyToOne
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    // ✅ réaction faite par un User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    public void onCreate() {
        this.creationDate = LocalDateTime.now();
    }
}