package tn.esprit.pi.service;

import tn.esprit.pi.domain.CommentReaction;
import tn.esprit.pi.domain.LikeType;
import java.util.List;

public interface ICommentReactionService {
    CommentReaction addReaction(Long commentId, Long userId, LikeType likeType);
    void removeReaction(Long reactionId);
    List<CommentReaction> getReactionsByComment(Long commentId);
}