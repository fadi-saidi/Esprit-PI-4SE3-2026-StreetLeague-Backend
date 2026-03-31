package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi.domain.PlayerStat;
import tn.esprit.pi.domain.SportType;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerStatRepository extends JpaRepository<PlayerStat, Long> {

    Optional<PlayerStat> findByPlayerIdAndWeekNumberAndWeekYear(
            Long playerId, int weekNumber, int weekYear);

    List<PlayerStat> findBySportTypeAndWeekNumberAndWeekYear(
            SportType sportType, int weekNumber, int weekYear);
}