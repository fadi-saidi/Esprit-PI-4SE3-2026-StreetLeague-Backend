package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi.domain.Injury;
import tn.esprit.pi.domain.InjurySeverity;

import java.util.List;

@Repository
public interface InjuryRepository extends JpaRepository<Injury, Long> {
    List<Injury> findByMedicalRecordId(Long medicalRecordId);
    List<Injury> findByMedicalRecordIdAndSeverity(Long medicalRecordId, InjurySeverity severity);
}