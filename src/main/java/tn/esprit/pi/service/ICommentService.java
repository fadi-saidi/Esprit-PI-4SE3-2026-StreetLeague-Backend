package tn.esprit.pi.service;

import tn.esprit.pi.domain.Comment;
import java.util.List;
import java.util.Optional;

public interface ICommentService {
    Comment createComment(Long postId, Long userId, String content);
    Comment replyToComment(Long parentCommentId, Long userId, String content);
    List<Comment> getCommentsByPost(Long postId);
    Optional<Comment> getCommentById(Long id);
    Comment updateComment(Long id, String newContent);
    void deleteComment(Long id);
    List<Comment> getRepliesByCommentId(Long commentId);
}