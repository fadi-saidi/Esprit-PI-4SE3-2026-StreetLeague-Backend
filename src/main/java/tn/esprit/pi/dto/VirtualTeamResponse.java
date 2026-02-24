package tn.esprit.pi.dto;

import lombok.Builder;
import lombok.Data;
import tn.esprit.pi.domain.SportType;

@Data
@Builder
public class VirtualTeamResponse {

    private Long id;
    private SportType sportType;
    private Double earnedPoints;
    private Double weekPoints;
    private Long userId;

}