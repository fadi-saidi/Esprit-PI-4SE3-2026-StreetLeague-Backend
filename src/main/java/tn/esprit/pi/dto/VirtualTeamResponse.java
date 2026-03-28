package tn.esprit.pi.dto;

import lombok.Builder;
import lombok.Data;
import tn.esprit.pi.domain.SportType;

import java.util.List;

@Data
@Builder
public class VirtualTeamResponse {

    private Long id;
    private String name;
    private SportType sportType;
    private Double earnedPoints;
    private Double weekPoints;
    private Long userId;
    private List<Long> playerIds;

}