package tn.esprit.pi.service.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.Dtos.InjuryDTO;
import tn.esprit.pi.dto.Dtos.MedicalRecordDTO;
import tn.esprit.pi.repository.HealthProfessionalProfileRepository;
import tn.esprit.pi.repository.InjuryRepository;
import tn.esprit.pi.repository.MedicalRecordRepository;
import tn.esprit.pi.repository.PlayerProfileRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - HealthServiceImpl")
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
    private HealthProfessionalProfile healthProfessional;
    private MedicalRecordDTO medicalRecordDTO;
    private InjuryDTO injuryDTO;

    @BeforeEach
    void setUp() {
        playerProfile = new PlayerProfile();
        playerProfile.setId(1L);

        healthProfessional = new HealthProfessionalProfile();
        healthProfessional.setId(10L);

        medicalRecord = new MedicalRecord();
        medicalRecord.setId(100L);
        medicalRecord.setWeight(75.5);
        medicalRecord.setHeight(180.0);
        medicalRecord.setBloodType("O+");
        medicalRecord.setPlayerProfile(playerProfile);

        medicalRecordDTO = new MedicalRecordDTO(
                null, 75.5, 180.0, "O+", "Asthme", "Pénicilline", LocalDate.now(), 1L, 10L
        );

        // Injury et DTO avec exactement les mêmes valeurs
        String report = "Douleur au genou";
        injury = new Injury();
        injury.setId(200L);
        injury.setReport(report);
        injury.setSeverity(InjurySeverity.MODERATE);
        injury.setDate(LocalDate.now());
        injury.setMedicalRecord(medicalRecord);

        injuryDTO = new InjuryDTO(
                null,
                report,           // même valeur que l'injury
                null,
                LocalDate.now(),
                InjurySeverity.MODERATE,
                100L
        );
    }

    @Test
    @DisplayName("Créer un dossier médical avec succès")
    void createRecord_Success() {
        when(playerProfileRepo.findById(1L)).thenReturn(Optional.of(playerProfile));
        when(healthProfRepo.findById(10L)).thenReturn(Optional.of(healthProfessional));
        when(medicalRecordRepo.save(any(MedicalRecord.class))).thenReturn(medicalRecord);

        MedicalRecordDTO result = healthService.createRecord(medicalRecordDTO);

        assertThat(result).isNotNull();
        assertThat(result.weight()).isEqualTo(75.5);
        verify(medicalRecordRepo).save(any(MedicalRecord.class));
    }

    @Test
    @DisplayName("Récupérer un dossier médical par ID")
    void getRecordById_Success() {
        when(medicalRecordRepo.findById(100L)).thenReturn(Optional.of(medicalRecord));

        MedicalRecordDTO result = healthService.getRecordById(100L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Déclarer une blessure avec succès")
    void declareInjury_Success() {
        when(medicalRecordRepo.findById(100L)).thenReturn(Optional.of(medicalRecord));
        when(injuryRepo.save(any(Injury.class))).thenReturn(injury);

        InjuryDTO result = healthService.declareInjury(injuryDTO);

        // Assertions plus claires
        assertThat(result).isNotNull();
        assertThat(result.report()).isEqualTo("Douleur au genou");
        assertThat(result.severity()).isEqualTo(InjurySeverity.MODERATE);
        verify(injuryRepo).save(any(Injury.class));
    }

    @Test
    @DisplayName("Filtrer les blessures par sévérité")
    void filterBySeverity_Success() {
        when(injuryRepo.findByMedicalRecordIdAndSeverity(100L, InjurySeverity.MODERATE))
                .thenReturn(List.of(injury));

        List<InjuryDTO> result = healthService.filterBySeverity(100L, InjurySeverity.MODERATE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).report()).isEqualTo("Douleur au genou");
    }
    @Test
    @DisplayName("Devrait lever une exception si le record est introuvable par ID")
    void getRecordById_NotFound_ThrowsException() {
        when(medicalRecordRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> healthService.getRecordById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Medical record not found");
    }

    @Test
    @DisplayName("Devrait lever une exception lors de la mise à jour d'un record inexistant")
    void updateRecord_NotFound_ThrowsException() {
        when(medicalRecordRepo.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> healthService.updateRecord(100L, medicalRecordDTO))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Suppression record - Cas succès et échec")
    void deleteRecord_Tests() {
        // Succès
        when(medicalRecordRepo.existsById(100L)).thenReturn(true);
        healthService.deleteRecord(100L);
        verify(medicalRecordRepo, times(1)).deleteById(100L);

        // Échec
        when(medicalRecordRepo.existsById(999L)).thenReturn(false);
        assertThatThrownBy(() -> healthService.deleteRecord(999L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Ajouter recommandation - Cas succès et échec")
    void addRecommendation_Tests() {
        when(injuryRepo.findById(200L)).thenReturn(Optional.of(injury));
        when(injuryRepo.save(any(Injury.class))).thenReturn(injury);

        InjuryDTO result = healthService.addRecommendation(200L, "Repos complet");

        assertThat(result).isNotNull();
        verify(injuryRepo).save(any(Injury.class));

        // Test de l'exception
        when(injuryRepo.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> healthService.addRecommendation(999L, "Repos"))
                .isInstanceOf(RuntimeException.class);
    }
}