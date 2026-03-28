package tn.esprit.pi.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostDto {
    private Long id;
    private String content;
    private LocalDateTime creationDate;
    private Long userId;
    private String username;
}