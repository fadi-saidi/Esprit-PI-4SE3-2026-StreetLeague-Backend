package tn.esprit.pi.dto;

import lombok.Data;
import tn.esprit.pi.domain.LikeType;

import java.time.LocalDateTime;

@Data
public class LikeDto {
    private Long id;
    private Long postId;
    private LikeType likeType;
    private Long userId;
    private LocalDateTime creationDate;
}