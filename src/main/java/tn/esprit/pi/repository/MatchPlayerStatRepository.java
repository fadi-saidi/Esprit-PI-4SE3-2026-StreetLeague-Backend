package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.domain.MatchPlayerStat;

import java.util.List;

public interface MatchPlayerStatRepository extends JpaRepository<MatchPlayerStat, Long> {
    List<MatchPlayerStat> findByMatchId(Long matchId);
}
