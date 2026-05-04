package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.domain.Comment;
import tn.esprit.pi.domain.Post;
import tn.esprit.pi.dto.CommentDto;
import tn.esprit.pi.dto.PostDto;
import tn.esprit.pi.repository.CommentRepository;
import tn.esprit.pi.repository.PostRepository;
import tn.esprit.pi.service.ICommentService;
import tn.esprit.pi.service.IPostService;
import tn.esprit.pi.service.NotificationService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/moderation")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ModerationController {

    private final IPostService          postService;
    private final ICommentService       commentService;
    private final NotificationService   notificationService;
    private final PostRepository        postRepository;
    private final CommentRepository     commentRepository;

    // ── Flagged lists ─────────────────────────────────────────────────────────

    @GetMapping("/flagged-posts")
    public ResponseEntity<List<PostDto>> getFlaggedPosts() {
        return ResponseEntity.ok(postService.getFlaggedPosts());
    }

    @GetMapping("/flagged-comments")
    public ResponseEntity<List<CommentDto>> getFlaggedComments() {
        return ResponseEntity.ok(
                commentService.getFlaggedComments().stream()
                        .map(this::toCommentDto)
                        .collect(Collectors.toList())
        );
    }

    /** Quick count endpoint — used by the sidebar badge */
    @GetMapping("/counts")
    public ResponseEntity<Map<String, Long>> getCounts() {
        long posts    = postService.getFlaggedPosts().size();
        long comments = commentService.getFlaggedComments().size();
        return ResponseEntity.ok(Map.of("flaggedPosts", posts, "flaggedComments", comments));
    }

    // ── Post moderation ───────────────────────────────────────────────────────

    @PutMapping("/posts/{id}/approve")
    public ResponseEntity<PostDto> approvePost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.approvePost(id));
    }

    @Transactional
    @PutMapping("/posts/{id}/reject")
    public ResponseEntity<Void> rejectPost(@PathVariable Long id) {
        // Read before deleting so we can notify the author
        Post post = postRepository.findById(id).orElse(null);
        if (post != null && post.getUser() != null) {
            notificationService.notifyPostRejected(post.getUser(), post.getContent());
        }
        postService.rejectPost(id);
        return ResponseEntity.noContent().build();
    }

    // ── Comment moderation ────────────────────────────────────────────────────

    @PutMapping("/comments/{id}/approve")
    public ResponseEntity<CommentDto> approveComment(@PathVariable Long id) {
        return ResponseEntity.ok(toCommentDto(commentService.approveComment(id)));
    }

    @Transactional
    @PutMapping("/comments/{id}/reject")
    public ResponseEntity<Void> rejectComment(@PathVariable Long id) {
        // Read before deleting so we can notify the author
        Comment comment = commentRepository.findById(id).orElse(null);
        if (comment != null && comment.getUser() != null) {
            notificationService.notifyCommentRejected(comment.getUser(), comment.getContent());
        }
        commentService.rejectComment(id);
        return ResponseEntity.noContent().build();
    }

    // ── Mapper ────────────────────────────────────────────────────────────────
    private CommentDto toCommentDto(Comment c) {
        CommentDto dto = new CommentDto();
        dto.setId(c.getId());
        dto.setContent(c.getContent());
        dto.setCreationDate(c.getCreationDate());
        dto.setUserId(c.getUser() != null ? c.getUser().getId() : null);
        dto.setUsername(c.getUser() != null ? c.getUser().getUsername() : "—");
        dto.setPostId(c.getPost() != null ? c.getPost().getId() : null);
        dto.setParentCommentId(c.getParentComment() != null ? c.getParentComment().getId() : null);
        dto.setFlagged(c.isFlagged());
        dto.setFlagReason(c.getFlagReason());
        return dto;
    }
}
