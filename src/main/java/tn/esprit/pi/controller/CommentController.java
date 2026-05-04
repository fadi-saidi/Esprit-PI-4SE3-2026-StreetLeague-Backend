package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.domain.Comment;
import tn.esprit.pi.domain.Post;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.dto.CommentDto;
import tn.esprit.pi.repository.CommentRepository;
import tn.esprit.pi.repository.PostRepository;
import tn.esprit.pi.repository.UserRepository;
import tn.esprit.pi.service.ICommentService;
import tn.esprit.pi.service.NotificationService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final ICommentService commentService;
    private final NotificationService notificationService;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @PostMapping
    public ResponseEntity<CommentDto> createComment(@RequestParam Long postId,
                                                    @RequestParam Long userId,
                                                    @RequestParam String content) {
        Comment saved = commentService.createComment(postId, userId, content);

        // Notify post owner — use fresh DB lookups, never navigate lazy fields
        try {
            Post post = postRepository.findById(postId).orElse(null);
            User commenter = userRepository.findById(userId).orElse(null);
            if (post != null && post.getUser() != null
                    && commenter != null
                    && !post.getUser().getId().equals(userId)) {
                notificationService.notifyPostCommented(post.getUser(), commenter.getUsername(), postId);
            }
        } catch (Exception e) {
            System.err.println("[CommentController] notification failed: " + e.getMessage());
        }

        return ResponseEntity.ok(toDto(saved));
    }

    @PostMapping("/reply")
    public ResponseEntity<CommentDto> replyToComment(@RequestParam Long parentCommentId,
                                                     @RequestParam Long userId,
                                                     @RequestParam String content) {
        Comment saved = commentService.replyToComment(parentCommentId, userId, content);

        // Notify parent comment owner — skip self-reply
        try {
            Comment parent = commentRepository.findById(parentCommentId).orElse(null);
            User replier = userRepository.findById(userId).orElse(null);
            if (parent != null && parent.getUser() != null
                    && replier != null
                    && !parent.getUser().getId().equals(userId)) {
                Long postId = parent.getPost() != null ? parent.getPost().getId() : null;
                notificationService.notifyCommentReplied(parent.getUser(), replier.getUsername(), postId);
            }
        } catch (Exception e) {
            System.err.println("[CommentController] reply notification failed: " + e.getMessage());
        }

        return ResponseEntity.ok(toDto(saved));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentDto>> getByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(
                commentService.getCommentsByPost(postId)
                        .stream()
                        .map(this::toDto)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentDto> getById(@PathVariable Long id) {
        return commentService.getCommentById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDto> update(@PathVariable Long id,
                                             @RequestParam String content) {
        return ResponseEntity.ok(toDto(commentService.updateComment(id, content)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/replies")
    public ResponseEntity<List<CommentDto>> getReplies(@PathVariable Long id) {
        return ResponseEntity.ok(
                commentService.getRepliesByCommentId(id)
                        .stream()
                        .map(this::toDto)
                        .collect(Collectors.toList())
        );
    }

    private CommentDto toDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreationDate(comment.getCreationDate());
        dto.setUserId(comment.getUser().getId());
        dto.setUsername(comment.getUser().getUsername());
        dto.setPostId(comment.getPost().getId());
        dto.setParentCommentId(
                comment.getParentComment() != null ? comment.getParentComment().getId() : null
        );
        return dto;
    }
}
