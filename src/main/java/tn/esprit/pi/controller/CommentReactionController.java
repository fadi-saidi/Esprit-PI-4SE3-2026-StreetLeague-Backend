package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.domain.CommentReaction;
import tn.esprit.pi.domain.LikeType;
import tn.esprit.pi.dto.CommentReactionDto;
import tn.esprit.pi.service.ICommentReactionService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/comment-reactions")
@RequiredArgsConstructor
public class CommentReactionController {

    private final ICommentReactionService reactionService;

    @PostMapping
    public ResponseEntity<CommentReactionDto> addReaction(
            @RequestParam Long commentId,
            @RequestParam Long userId,
            @RequestParam LikeType likeType) {
        return ResponseEntity.ok(toDto(reactionService.addReaction(commentId, userId, likeType)));
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