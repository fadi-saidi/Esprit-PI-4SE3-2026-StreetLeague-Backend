package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.domain.EventStatus;
import tn.esprit.pi.domain.Match;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByWeekNumberAndWeekYear(int weekNumber, int weekYear);
    List<Match> findByWeekYear(int weekYear);

    /** All FINISHED matches for a given fantasy week — used by the scheduler */
    List<Match> findByStatusAndWeekNumberAndWeekYear(EventStatus status, int weekNumber, int weekYear);
}
