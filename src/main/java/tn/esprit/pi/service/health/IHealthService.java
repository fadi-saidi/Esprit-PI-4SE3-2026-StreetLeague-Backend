package tn.esprit.pi.service.health;

import tn.esprit.pi.domain.InjurySeverity;
import tn.esprit.pi.dto.Dtos.InjuryDTO;
import tn.esprit.pi.dto.Dtos.MedicalRecordDTO;

import java.util.List;

public interface IHealthService {


    MedicalRecordDTO createRecord(MedicalRecordDTO dto);

    // Player or healthProfessionnal updates
    MedicalRecordDTO updateRecord(Long id, MedicalRecordDTO dto);

    // Admin deletes a medical record
    void deleteRecord(Long id);

    // Get a record by its own ID
    MedicalRecordDTO getRecordById(Long id);

    // Get a player's record using their player profile ID — used by coach/doctor
    MedicalRecordDTO getRecordByPlayer(Long playerProfileId);

    // Player declares a new injury
    InjuryDTO declareInjury(InjuryDTO dto);

    // Health professional adds medical advice to an injury (backlog #30)
    InjuryDTO addRecommendation(Long injuryId, String recommendation);

    // Delete an injury record
    void deleteInjury(Long id);

    // Get all injuries of a player's medical record — used by coach (backlog #32)
    List<InjuryDTO> getInjuriesByRecord(Long medicalRecordId);

    // Filter injuries by severity: MINOR, MODERATE, SEVERE (backlog #34)
    List<InjuryDTO> filterBySeverity(Long medicalRecordId, InjurySeverity severity);

    // Get a single injury by ID
    InjuryDTO getInjuryById(Long id);
}