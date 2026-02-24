package tn.esprit.pi.repository;

import tn.esprit.pi.entity.Evenement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EvenementRepository extends JpaRepository<Evenement, Long> {
    List<Evenement> findByTournoiId(Long tournoiId);
    List<Evenement> findByDateGreaterThanEqual(LocalDate date);
}