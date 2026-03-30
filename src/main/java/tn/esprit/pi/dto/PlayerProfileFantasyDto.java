package tn.esprit.pi.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerProfileFantasyDto {
    private Long    id;
    private String  firstName;
    private String  lastName;
    private String  position;
    private String  sportType;
    private String  level;
    private Double  avgRating;
    private Integer fantasyPoints;
    private Integer goalsScored;
    private Integer assists;
    private Integer matchesPlayed;
}