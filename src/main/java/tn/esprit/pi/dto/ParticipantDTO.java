package tn.esprit.pi.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantDTO {
    private Long userId;
    private String username;
    private String email;
    private String phone;
}