package tn.esprit.pi.repository;

import tn.esprit.pi.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {
    List<Training> findByTournoiId(Long tournoiId);
    List<Training> findByCoachIgnoreCase(String coach);
}
