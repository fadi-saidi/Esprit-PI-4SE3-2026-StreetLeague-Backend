package tn.esprit.pi.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeamDTO {

    private Long id;

    @NotBlank(message = "Team name is required")
    @Size(min = 2, max = 80, message = "Name must be between 2 and 80 characters")
    private String name;

    private String logo;

    @NotBlank(message = "Sport type is required")
    private String type;          // SportType enum as String — matches frontend field name

    private Long   captainId;
    private String captainName;

    private Long   coachId;
    private String coachName;

    private List<PlayerSummaryDTO> players;
    private int    playerCount;

    private LocalDateTime createdAt;
}
