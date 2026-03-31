package tn.esprit.pi.domain;

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
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String content;
    private LocalDateTime creationDate;
    @PrePersist
    public void onCreate() {
        this.creationDate = LocalDateTime.now();
    }
    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> replies = new HashSet<>();
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CommentReaction> reactions = new HashSet<>();

    public void addReply(Comment reply) {
        reply.setParentComment(this);
        reply.setPost(this.post);
        this.replies.add(reply);
    }

    public void removeReply(Comment reply) {
        reply.setParentComment(null);
        this.replies.remove(reply);
    }
    public void addReaction(CommentReaction reaction) {
        reaction.setComment(this);   // FK comment_id remplie
        this.reactions.add(reaction);
    }
    public void removeReaction(CommentReaction reaction) {
        reaction.setComment(null);
        this.reactions.remove(reaction);
    }
}
