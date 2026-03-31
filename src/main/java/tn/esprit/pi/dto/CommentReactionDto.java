package tn.esprit.pi.dto;

import lombok.Data;
import tn.esprit.pi.domain.LikeType;
import java.time.LocalDateTime;

@Data
public class CommentReactionDto {
    private Long id;
    private LikeType likeType;
    private LocalDateTime creationDate;
    private Long commentId;
    private Long userId;
    private String username;
}