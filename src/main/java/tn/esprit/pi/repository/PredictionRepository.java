package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pi.domain.Prediction;
import tn.esprit.pi.domain.PredictionStatus;

import java.util.List;
import java.util.Optional;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    // Find the PENDING prediction for a team in a specific week (for reuse/edit)
    Optional<Prediction> findByVirtualTeamIdAndWeekNumberAndWeekYearAndStatus(
            Long virtualTeamId, int weekNumber, int weekYear, PredictionStatus status);

    // Find the latest prediction for a team in a specific week (for current view)
    Optional<Prediction> findFirstByVirtualTeamIdAndWeekNumberAndWeekYearOrderByIdDesc(
            Long virtualTeamId, int weekNumber, int weekYear);

    // All pending predictions for a given week (scheduler uses this)
    List<Prediction> findByStatusAndWeekNumberAndWeekYear(
            PredictionStatus status, int weekNumber, int weekYear);

    // All predictions for a team (history)
    List<Prediction> findByVirtualTeamIdOrderByWeekYearDescWeekNumberDesc(Long virtualTeamId);

    void deleteByVirtualTeamId(Long virtualTeamId);

    // All predictions by status (admin: list all pending)
    List<Prediction> findByStatusOrderByWeekYearDescWeekNumberDesc(PredictionStatus status);

    // All predictions regardless of status (admin: full list)
    List<Prediction> findAllByOrderByWeekYearDescWeekNumberDesc();

    // ── Stats queries ─────────────────────────────────────────────────────────

    /** Sum of totalPointsEarned for all RESOLVED predictions in a given week */
    @Query("SELECT COALESCE(SUM(p.totalPointsEarned), 0) FROM Prediction p " +
           "WHERE p.status = 'RESOLVED' AND p.weekNumber = :weekNumber AND p.weekYear = :weekYear")
    Double sumPointsThisWeek(int weekNumber, int weekYear);

    /** Player name who appears most across all player_prediction rows */
    @Query(value = "SELECT pp.player_name FROM player_prediction pp " +
                   "GROUP BY pp.player_name ORDER BY COUNT(*) DESC LIMIT 1",
           nativeQuery = true)
    String mostPredictedPlayerName();

    /** Best resolved prediction this week: [virtualTeamId, totalPointsEarned] */
    @Query("SELECT p.virtualTeam.id, p.totalPointsEarned FROM Prediction p " +
           "WHERE p.status = 'RESOLVED' AND p.weekNumber = :weekNumber AND p.weekYear = :weekYear " +
           "ORDER BY p.totalPointsEarned DESC")
    List<Object[]> topPredictionThisWeek(int weekNumber, int weekYear);
    
    @Query("SELECT p FROM Prediction p " +
            "JOIN p.virtualTeam vt " +
            "JOIN vt.user u " +
            "WHERE u.id = :userId " +
            "AND p.weekNumber = :weekNumber " +
            "AND p.weekYear = :weekYear")
    List<Prediction> findByUserAndWeek(
            @Param("userId") Long userId,
            @Param("weekNumber") int weekNumber,
            @Param("weekYear") int weekYear
    );
}


