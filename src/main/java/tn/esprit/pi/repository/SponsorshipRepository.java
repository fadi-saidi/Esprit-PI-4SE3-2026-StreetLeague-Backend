package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pi.domain.*;

import java.time.LocalDate;
import java.util.List;

public interface SponsorshipRepository extends JpaRepository<Sponsorship, Long> {
    List<Sponsorship> findBySponsorProfile(SponsorProfile sponsorProfile);
    List<Sponsorship> findByStatus(SponsorshipStatus status);
    List<Sponsorship> findByTeamId(Long teamId);
    List<Sponsorship> findByEventId(Long eventId);
    List<Sponsorship> findByVenueId(Long venueId);
    
    @Query("SELECT COUNT(s), SUM(s.amount) FROM Sponsorship s WHERE s.status = :status AND s.startDate >= :startDate")
    List<Object[]> getStatsForPeriod(@Param("status") SponsorshipStatus status, @Param("startDate") LocalDate startDate);
}