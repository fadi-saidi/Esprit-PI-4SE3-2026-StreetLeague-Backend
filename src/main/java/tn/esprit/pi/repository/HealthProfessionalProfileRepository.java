package tn.esprit.pi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi.domain.HealthProfessionalProfile;

import java.util.Optional;

@Repository
public interface HealthProfessionalProfileRepository extends JpaRepository<HealthProfessionalProfile, Long> {

    // Used by UserProfileHelper (already exists in your project)
    Optional<HealthProfessionalProfile> findByUserId(Long userId);

    // Used by HealthServiceImpl to validate a health professional
    java.util.List<HealthProfessionalProfile> findByVerified(Boolean verified);
}