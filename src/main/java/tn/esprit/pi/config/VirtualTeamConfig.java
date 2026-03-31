package tn.esprit.pi.config;
import tn.esprit.pi.domain.SportType;
import java.util.Map;
public class VirtualTeamConfig {

    // Number of starters and subs per sport
    public static final Map<SportType, Integer> MAX_STARTERS = Map.of(
            SportType.FOOTBALL, 11,
            SportType.BASKETBALL, 5,
            SportType.TENNIS, 1
    );

    public static final Map<SportType, Integer> MAX_SUBSTITUTES = Map.of(
            SportType.FOOTBALL, 4,
            SportType.BASKETBALL, 2,
            SportType.TENNIS, 2
    );
}