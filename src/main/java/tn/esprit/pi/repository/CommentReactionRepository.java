package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.domain.CommentReaction;
import java.util.List;

public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {
    List<CommentReaction> findByCommentId(Long commentId);

    List<CommentReaction> findByUserId(Long userId);
}