package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.dto.CommentDto;
import tn.esprit.pi.service.ICommentService;
import tn.esprit.pi.domain.Comment;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final ICommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto> createComment(@RequestParam Long postId,
                                                    @RequestParam Long userId,
                                                    @RequestParam String content) {
        return ResponseEntity.ok(toDto(commentService.createComment(postId, userId, content)));
    }

    @PostMapping("/reply")
    public ResponseEntity<CommentDto> replyToComment(@RequestParam Long parentCommentId,
                                                     @RequestParam Long userId,
                                                     @RequestParam String content) {
        return ResponseEntity.ok(toDto(commentService.replyToComment(parentCommentId, userId, content)));
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
    // GET /comments/{id}/replies
    @GetMapping("/{id}/replies")
    public ResponseEntity<List<CommentDto>> getReplies(@PathVariable Long id) {
        return ResponseEntity.ok(
                commentService.getRepliesByCommentId(id)
                        .stream()
                        .map(this::toDto)
                        .collect(Collectors.toList())
        );
    }
    // ✅ mapper
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