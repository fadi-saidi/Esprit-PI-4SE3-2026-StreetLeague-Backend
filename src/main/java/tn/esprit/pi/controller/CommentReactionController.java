package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.domain.Comment;
import tn.esprit.pi.domain.CommentReaction;
import tn.esprit.pi.domain.LikeType;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.dto.CommentReactionDto;
import tn.esprit.pi.repository.CommentRepository;
import tn.esprit.pi.repository.UserRepository;
import tn.esprit.pi.service.ICommentReactionService;
import tn.esprit.pi.service.NotificationService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/comment-reactions")
@RequiredArgsConstructor
public class CommentReactionController {

    private final ICommentReactionService reactionService;
    private final NotificationService notificationService;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<CommentReactionDto> addReaction(
            @RequestParam Long commentId,
            @RequestParam Long userId,
            @RequestParam LikeType likeType) {

        CommentReaction saved = reactionService.addReaction(commentId, userId, likeType);

        // Notify comment owner — skip self-reaction
        try {
            Comment comment = commentRepository.findById(commentId).orElse(null);
            User liker = userRepository.findById(userId).orElse(null);
            if (comment != null && comment.getUser() != null
                    && liker != null
                    && !comment.getUser().getId().equals(userId)) {
                Long postId = comment.getPost() != null ? comment.getPost().getId() : null;
                notificationService.notifyCommentLiked(comment.getUser(), liker.getUsername(), postId);
            }
        } catch (Exception e) {
            System.err.println("[CommentReactionController] notification failed: " + e.getMessage());
        }

        return ResponseEntity.ok(toDto(saved));
    }

    @GetMapping("/comment/{commentId}")
    public ResponseEntity<List<CommentReactionDto>> getByComment(@PathVariable Long commentId) {
        return ResponseEntity.ok(
                reactionService.getReactionsByComment(commentId)
                        .stream()
                        .map(this::toDto)
                        .collect(Collectors.toList())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeReaction(@PathVariable Long id) {
        reactionService.removeReaction(id);
        return ResponseEntity.noContent().build();
    }

    private CommentReactionDto toDto(CommentReaction r) {
        CommentReactionDto dto = new CommentReactionDto();
        dto.setId(r.getId());
        dto.setLikeType(r.getLikeType());
        dto.setCreationDate(r.getCreationDate());
        dto.setCommentId(r.getComment().getId());
        dto.setUserId(r.getUser().getId());
        dto.setUsername(r.getUser().getUsername());
        return dto;
    }
}
