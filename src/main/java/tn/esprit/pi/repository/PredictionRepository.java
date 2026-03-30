package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.domain.Prediction;
import tn.esprit.pi.domain.PredictionStatus;

import java.util.List;
import java.util.Optional;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    // Find prediction for a team in a specific week
    Optional<Prediction> findByVirtualTeamIdAndWeekNumberAndWeekYear(
            Long virtualTeamId, int weekNumber, int weekYear);

    // All pending predictions for a given week (scheduler uses this)
    List<Prediction> findByStatusAndWeekNumberAndWeekYear(
            PredictionStatus status, int weekNumber, int weekYear);

    // All predictions for a team (history)
    List<Prediction> findByVirtualTeamIdOrderByWeekYearDescWeekNumberDesc(Long virtualTeamId);

    void deleteByVirtualTeamId(Long virtualTeamId);
}


