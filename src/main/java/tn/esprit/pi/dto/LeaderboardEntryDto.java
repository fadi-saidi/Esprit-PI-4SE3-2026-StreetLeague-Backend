package tn.esprit.pi.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LeaderboardEntryDto {
    private int rank;
    private String username;
    private String email;
    private int walletPoints;
}
