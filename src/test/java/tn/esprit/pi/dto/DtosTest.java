package tn.esprit.pi.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tn.esprit.pi.domain.InjurySeverity;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests - Dtos (Records)")
class DtosTest {

    @Test
    @DisplayName("TransferRequest - Intégrité des données")
    void testTransferRequest() {
        // Correction : utilise recipientId au lieu de toUserId
        Dtos.TransferRequest req = new Dtos.TransferRequest(7L, 50.0, "Lunch");

        assertEquals(7L, req.recipientId());
        assertEquals(50.0, req.amount());
    }

    @Test
    @DisplayName("MedicalRecordDTO - Validation des limites")
    void testMedicalRecordDTO() {
        LocalDate now = LocalDate.now();
        Dtos.MedicalRecordDTO dto = new Dtos.MedicalRecordDTO(
                1L, 75.5, 180.0, "O+", "None", "Peanuts", now, 10L, 20L
        );
        assertEquals(75.5, dto.weight());
        assertEquals("O+", dto.bloodType());
    }
}