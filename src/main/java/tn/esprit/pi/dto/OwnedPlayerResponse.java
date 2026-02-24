package tn.esprit.pi.dto;

import lombok.Builder;
import lombok.Data;
import tn.esprit.pi.domain.PlayerStatus;

import java.time.LocalDate;

@Data
@Builder
public class OwnedPlayerResponse {
    private Long id;
    private Long userId;
    private Long teamId;
    private Long playerProfileId;
    private PlayerStatus status;
    private LocalDate acquiredDate;
}