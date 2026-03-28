package tn.esprit.pi.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentDto {
    private Long id;
    private String content;
    private LocalDateTime creationDate;
    private Long userId;
    private String username;
    private Long postId;
    private Long parentCommentId;
    private List<CommentDto> replies;
}