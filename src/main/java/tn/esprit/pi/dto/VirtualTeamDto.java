package tn.esprit.pi.dto;

import lombok.Data;
import tn.esprit.pi.domain.SportType;

import java.util.List;

@Data
public class VirtualTeamDto {

    private SportType sportType;
    private Long userId;
    private List<Long> playerIds;
    private Double earnedPoints;
    private Double weekPoints;
}