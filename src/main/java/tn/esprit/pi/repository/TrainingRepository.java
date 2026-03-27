package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.domain.Training;

public interface TrainingRepository extends JpaRepository<Training, Long> {
}
