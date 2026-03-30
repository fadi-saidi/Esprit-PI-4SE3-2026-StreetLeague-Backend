package tn.esprit.pi.dto;

import lombok.*;

/**
 * DTO for JoinRequest — shape kept compatible with Angular frontend:
 *   req.team.id / req.team.name
 *   req.player.id / req.player.fullName / req.player.email
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JoinRequestDTO {

    private Long id;

    // Minimal team info for frontend filtering
    private TeamRef team;

    // Minimal player info for display
    private PlayerSummaryDTO player;

    private String type;    // REQUEST | INVITATION
    private String status;  // PENDING | ACCEPTED | REFUSED

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TeamRef {
        private Long   id;
        private String name;
        private String type;      // SportType as String
        private Long   captainId;
    }
}
