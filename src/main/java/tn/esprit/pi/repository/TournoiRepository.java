package tn.esprit.pi.repository;

import tn.esprit.pi.entity.Tournoi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TournoiRepository extends JpaRepository<Tournoi, Long> {
    List<Tournoi> findByLieuIgnoreCase(String lieu);
    List<Tournoi> findByDateDebutGreaterThanEqual(LocalDate date);
}
