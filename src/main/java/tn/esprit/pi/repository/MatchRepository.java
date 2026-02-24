package tn.esprit.pi.repository;

import tn.esprit.pi.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByTournoiId(Long tournoiId);
    List<Match> findByEquipeAIgnoreCaseOrEquipeBIgnoreCase(String equipeA, String equipeB);
}