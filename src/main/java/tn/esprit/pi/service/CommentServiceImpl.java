package tn.esprit.pi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.repository.*;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements ICommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final BadWordsService badWordsService;

    @Override
    public Comment createComment(Long postId, Long userId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Comment comment = new Comment();
        comment.setContent(content);

        if (badWordsService.containsBadWords(content)) {
            comment.setFlagged(true);
            comment.setFlagReason("bad_words");
        }

        post.addComment(comment);
        user.addComment(comment);

        return commentRepository.save(comment);
    }
    @Override
    public Comment replyToComment(Long parentCommentId, Long userId, String content) {

        //  1 — charger le Comment parent depuis BDD
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new RuntimeException("Comment not found: " + parentCommentId));

        // 2 — charger le User depuis BDD
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // 3 — créer la réponse
        Comment reply = new Comment();
        reply.setContent(content);
        reply.setUser(user);

        if (badWordsService.containsBadWords(content)) {
            reply.setFlagged(true);
            reply.setFlagReason("bad_words");
        }

        parentComment.addReply(reply);
        return commentRepository.save(reply);
    }

    @Override
    public List<Comment> getCommentsByPost(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    @Override
    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    @Override
    public Comment updateComment(Long id, String newContent) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found: " + id));

        //  seulement le contenu change — on ne touche pas aux relations
        comment.setContent(newContent);
        return commentRepository.save(comment);
    }

    @Override
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found: " + id));

        // PARENT (Post) retire proprement l'enfant (Comment)
        // Sans ça → orphanRemoval peut ne pas se déclencher correctement
        comment.getPost().removeComment(comment);

        commentRepository.delete(comment);
    }

    @Override
    public List<Comment> getRepliesByCommentId(Long commentId) {
        return commentRepository.findByParentCommentId(commentId);
    }

    // ── Moderation ────────────────────────────────────────────────────────────
    @Override
    public List<Comment> getFlaggedComments() {
        return commentRepository.findByFlaggedTrue();
    }

    @Override
    public Comment approveComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found: " + id));
        comment.setFlagged(false);
        comment.setFlagReason(null);
        return commentRepository.save(comment);
    }

    @Override
    public void rejectComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found: " + id));
        comment.getPost().removeComment(comment);
        commentRepository.delete(comment);
    }
}