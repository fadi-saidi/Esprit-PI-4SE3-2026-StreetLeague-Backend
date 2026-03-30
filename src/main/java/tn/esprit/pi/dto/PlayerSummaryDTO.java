package tn.esprit.pi.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlayerSummaryDTO {
    private Long id;
    private String fullName;
    private String email;
    private String level;   // PlayerLevel as String
}
