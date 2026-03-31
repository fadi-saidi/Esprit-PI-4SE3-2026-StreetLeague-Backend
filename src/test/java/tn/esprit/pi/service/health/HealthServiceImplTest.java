package tn.esprit.pi.service.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.Dtos.InjuryDTO;
import tn.esprit.pi.dto.Dtos.MedicalRecordDTO;
import tn.esprit.pi.repository.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthServiceImplTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepo;

    @Mock
    private InjuryRepository injuryRepo;

    @Mock
    private PlayerProfileRepository playerProfileRepo;

    @Mock
    private HealthProfessionalProfileRepository healthProfRepo;

    @InjectMocks
    private HealthServiceImpl healthService;

    private MedicalRecord medicalRecord;
    private Injury injury;
    private PlayerProfile playerProfile;
    private HealthProfessionalProfile healthProf;

    @BeforeEach
    void setUp() {
        playerProfile = new PlayerProfile();
        playerProfile.setId(1L);

        healthProf = new HealthProfessionalProfile();
        healthProf.setId(1L);

        medicalRecord = new MedicalRecord();
        medicalRecord.setId(1L);
        medicalRecord.setWeight(70.0);
        medicalRecord.setHeight(175.0);
        medicalRecord.setPlayerProfile(playerProfile);
        medicalRecord.setHealthProfessionalProfile(healthProf);

        injury = new Injury();
        injury.setId(1L);
        injury.setReport("Sprained ankle");
        injury.setSeverity(InjurySeverity.MODERATE);
        injury.setMedicalRecord(medicalRecord);
    }

    @Test
    void createRecord_ShouldCreateAndReturnDTO() {
        // Arrange
        MedicalRecordDTO dto = new MedicalRecordDTO(null, 70.0, 175.0, "A+", null, null, LocalDate.now(), 1L, 1L);
        when(playerProfileRepo.findById(1L)).thenReturn(Optional.of(playerProfile));
        when(healthProfRepo.findById(1L)).thenReturn(Optional.of(healthProf));
        when(medicalRecordRepo.save(any(MedicalRecord.class))).thenReturn(medicalRecord);

        // Act
        MedicalRecordDTO result = healthService.createRecord(dto);

        // Assert
        assertNotNull(result);
        assertEquals(70.0, result.weight());
        verify(medicalRecordRepo).save(any(MedicalRecord.class));
    }

    @Test
    void updateRecord_ShouldUpdateAndReturnDTO() {
        // Arrange
        MedicalRecordDTO dto = new MedicalRecordDTO(1L, 75.0, 180.0, "B+", null, null, LocalDate.now(), 1L, 1L);
        when(medicalRecordRepo.findById(1L)).thenReturn(Optional.of(medicalRecord));
        when(playerProfileRepo.findById(1L)).thenReturn(Optional.of(playerProfile));
        when(healthProfRepo.findById(1L)).thenReturn(Optional.of(healthProf));
        when(medicalRecordRepo.save(any(MedicalRecord.class))).thenReturn(medicalRecord);

        // Act
        MedicalRecordDTO result = healthService.updateRecord(1L, dto);

        // Assert
        assertNotNull(result);
        verify(medicalRecordRepo).save(medicalRecord);
    }

    @Test
    void deleteRecord_ShouldDeleteIfExists() {
        // Arrange
        when(medicalRecordRepo.existsById(1L)).thenReturn(true);

        // Act
        healthService.deleteRecord(1L);

        // Assert
        verify(medicalRecordRepo).deleteById(1L);
    }

    @Test
    void deleteRecord_ShouldThrowExceptionIfNotExists() {
        // Arrange
        when(medicalRecordRepo.existsById(1L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> healthService.deleteRecord(1L));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void getRecordById_ShouldReturnDTO() {
        // Arrange
        when(medicalRecordRepo.findById(1L)).thenReturn(Optional.of(medicalRecord));

        // Act
        MedicalRecordDTO result = healthService.getRecordById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void declareInjury_ShouldCreateAndReturnDTO() {
        // Arrange
        InjuryDTO dto = new InjuryDTO(null, "Sprained ankle", null, LocalDate.now(), InjurySeverity.MODERATE, 1L);
        when(medicalRecordRepo.findById(1L)).thenReturn(Optional.of(medicalRecord));
        when(injuryRepo.save(any(Injury.class))).thenReturn(injury);

        // Act
        InjuryDTO result = healthService.declareInjury(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Sprained ankle", result.report());
        verify(injuryRepo).save(any(Injury.class));
    }

    @Test
    void addRecommendation_ShouldUpdateInjury() {
        // Arrange
        when(injuryRepo.findById(1L)).thenReturn(Optional.of(injury));
        when(injuryRepo.save(any(Injury.class))).thenReturn(injury);

        // Act
        InjuryDTO result = healthService.addRecommendation(1L, "Rest for 2 weeks");

        // Assert
        assertNotNull(result);
        verify(injuryRepo).save(injury);
    }

    @Test
    void getInjuriesByRecord_ShouldReturnList() {
        // Arrange
        List<Injury> injuries = Arrays.asList(injury);
        when(injuryRepo.findByMedicalRecordId(1L)).thenReturn(injuries);

        // Act
        List<InjuryDTO> result = healthService.getInjuriesByRecord(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllRecords_ShouldReturnList() {
        // Arrange
        List<MedicalRecord> records = Arrays.asList(medicalRecord);
        when(medicalRecordRepo.findAll()).thenReturn(records);

        // Act
        List<MedicalRecordDTO> result = healthService.getAllRecords();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
