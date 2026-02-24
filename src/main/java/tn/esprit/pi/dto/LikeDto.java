package tn.esprit.pi.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LikeDto {
    private Long id;
    private Long postId;
    private Long userId;
    private LocalDateTime creationDate;
}