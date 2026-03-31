package tn.esprit.pi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.repository.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentReactionServiceImpl implements ICommentReactionService {

    private final CommentReactionRepository reactionRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    public CommentReaction addReaction(Long commentId, Long userId, LikeType likeType) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found: " + commentId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        CommentReaction reaction = new CommentReaction();
        reaction.setLikeType(likeType);

        //  DOUBLE affectation
        comment.addReaction(reaction);       // FK comment_id remplie
        user.addCommentReaction(reaction);   // FK user_id remplie

        return reactionRepository.save(reaction);
    }

    @Override
    public void removeReaction(Long reactionId) {
        CommentReaction reaction = reactionRepository.findById(reactionId)
                .orElseThrow(() -> new RuntimeException("Reaction not found: " + reactionId));

        // PARENT retire proprement l'enfant
        reaction.getComment().removeReaction(reaction);

        reactionRepository.delete(reaction);
    }

    @Override
    public List<CommentReaction> getReactionsByComment(Long commentId) {
        return reactionRepository.findByCommentId(commentId);
    }
}