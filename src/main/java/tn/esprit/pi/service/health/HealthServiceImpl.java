package tn.esprit.pi.service.health;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.HealthProfessionalProfile;
import tn.esprit.pi.domain.Injury;
import tn.esprit.pi.domain.InjurySeverity;
import tn.esprit.pi.domain.MedicalRecord;
import tn.esprit.pi.domain.PlayerProfile;
import tn.esprit.pi.dto.Dtos.InjuryDTO;
import tn.esprit.pi.dto.Dtos.MedicalRecordDTO;
import tn.esprit.pi.repository.HealthProfessionalProfileRepository;
import tn.esprit.pi.repository.InjuryRepository;
import tn.esprit.pi.repository.MedicalRecordRepository;
import tn.esprit.pi.repository.PlayerProfileRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HealthServiceImpl implements IHealthService {

    private final MedicalRecordRepository medicalRecordRepo;
    private final InjuryRepository injuryRepo;
    private final PlayerProfileRepository playerProfileRepo;
    private final HealthProfessionalProfileRepository healthProfRepo;

    // ----------------------------------------------------------
    //  Medical Record — CRUD
    // ----------------------------------------------------------

    @Override
    public MedicalRecordDTO createRecord(MedicalRecordDTO dto) {
        MedicalRecord record = new MedicalRecord();
        mapDtoToEntity(dto, record);
        return toDTO(medicalRecordRepo.save(record));
    }

    @Override
    public MedicalRecordDTO updateRecord(Long id, MedicalRecordDTO dto) {
        MedicalRecord record = medicalRecordRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical record not found with id: " + id));
        mapDtoToEntity(dto, record);
        return toDTO(medicalRecordRepo.save(record));
    }

    @Override
    public void deleteRecord(Long id) {
        if (!medicalRecordRepo.existsById(id))
            throw new RuntimeException("Medical record not found with id: " + id);
        medicalRecordRepo.deleteById(id);
    }

    @Override
    public MedicalRecordDTO getRecordById(Long id) {
        return toDTO(medicalRecordRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical record not found with id: " + id)));
    }

    @Override
    public MedicalRecordDTO getRecordByPlayer(Long playerProfileId) {
        return toDTO(medicalRecordRepo.findByPlayerProfileId(playerProfileId)
                .orElseThrow(() -> new RuntimeException(
                        "No medical record found for player: " + playerProfileId)));
    }

    // ----------------------------------------------------------
    //  Injury — CRUD
    // ----------------------------------------------------------

    @Override
    public InjuryDTO declareInjury(InjuryDTO dto) {
        MedicalRecord record = medicalRecordRepo.findById(dto.medicalRecordId())
                .orElseThrow(() -> new RuntimeException(
                        "Medical record not found with id: " + dto.medicalRecordId()));

        Injury injury = new Injury();
        injury.setReport(dto.report());
        injury.setDate(dto.date());
        injury.setSeverity(dto.severity());
        injury.setMedicalRecord(record);

        return toInjuryDTO(injuryRepo.save(injury));
    }

    @Override
    public InjuryDTO addRecommendation(Long injuryId, String recommendation) {
        Injury injury = injuryRepo.findById(injuryId)
                .orElseThrow(() -> new RuntimeException("Injury not found with id: " + injuryId));
        injury.setRecommendation(recommendation);
        return toInjuryDTO(injuryRepo.save(injury));
    }

    @Override
    public void deleteInjury(Long id) {
        if (!injuryRepo.existsById(id))
            throw new RuntimeException("Injury not found with id: " + id);
        injuryRepo.deleteById(id);
    }

    @Override
    public List<InjuryDTO> getInjuriesByRecord(Long medicalRecordId) {
        return injuryRepo.findByMedicalRecordId(medicalRecordId)
                .stream()
                .map(this::toInjuryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InjuryDTO> filterBySeverity(Long medicalRecordId, InjurySeverity severity) {
        return injuryRepo.findByMedicalRecordIdAndSeverity(medicalRecordId, severity)
                .stream()
                .map(this::toInjuryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InjuryDTO getInjuryById(Long id) {
        return toInjuryDTO(injuryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Injury not found with id: " + id)));
    }

    @Override
    public List<InjuryDTO> getAllInjuries() {
        return injuryRepo.findAll()
                .stream()
                .map(this::toInjuryDTO)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------
    //  Private helpers
    // ----------------------------------------------------------

    private void mapDtoToEntity(MedicalRecordDTO dto, MedicalRecord record) {
        record.setWeight(dto.weight());
        record.setHeight(dto.height());
        record.setBloodType(dto.bloodType());
        record.setChronicDiseases(dto.chronicDiseases());
        record.setAllergies(dto.allergies());
        record.setLastCheckup(dto.lastCheckup());

        if (dto.playerProfileId() != null) {
            PlayerProfile player = playerProfileRepo.findById(dto.playerProfileId())
                    .orElseThrow(() -> new RuntimeException(
                            "Player profile not found: " + dto.playerProfileId()));
            record.setPlayerProfile(player);
        }

        if (dto.healthProfessionalId() != null) {
            HealthProfessionalProfile prof = healthProfRepo.findById(dto.healthProfessionalId())
                    .orElseThrow(() -> new RuntimeException(
                            "Health professional not found: " + dto.healthProfessionalId()));
            record.setHealthProfessionalProfile(prof);
        }
    }

    private MedicalRecordDTO toDTO(MedicalRecord r) {
        return new MedicalRecordDTO(
                r.getId(),
                r.getWeight(),
                r.getHeight(),
                r.getBloodType(),
                r.getChronicDiseases(),
                r.getAllergies(),
                r.getLastCheckup(),
                r.getPlayerProfile() != null ? r.getPlayerProfile().getId() : null,
                r.getHealthProfessionalProfile() != null
                        ? r.getHealthProfessionalProfile().getId() : null
        );
    }

    private InjuryDTO toInjuryDTO(Injury i) {
        return new InjuryDTO(
                i.getId(),
                i.getReport(),
                i.getRecommendation(),
                i.getDate(),
                i.getSeverity(),
                i.getMedicalRecord() != null ? i.getMedicalRecord().getId() : null
        );
    }

    @Override
    public List<MedicalRecordDTO> getAllRecords() {
        return medicalRecordRepo.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}