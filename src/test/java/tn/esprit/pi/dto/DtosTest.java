package tn.esprit.pi.dto;

import org.junit.jupiter.api.Test;
import tn.esprit.pi.domain.Role;
import tn.esprit.pi.dto.Dtos.*;

import static org.junit.jupiter.api.Assertions.*;

class DtosTest {

    @Test
    void registerRequest_ShouldCreateWithAllFields() {
        // Act
        RegisterRequest request = new RegisterRequest(
                "John Doe",
                "john@example.com",
                "password123",
                Role.PLAYER,
                "1990-01-01",
                "UEFA A License",
                "12345",
                "Sports Medicine",
                5,
                "Nike Inc",
                "logo.png",
                "contact@nike.com",
                50000.0,
                "+216 12 345 678"
        );

        // Assert
        assertEquals("John Doe", request.fullName());
        assertEquals("john@example.com", request.email());
        assertEquals("password123", request.password());
        assertEquals(Role.PLAYER, request.role());
        assertEquals("1990-01-01", request.dateOfBirth());
        assertEquals("+216 12 345 678", request.phone());
    }

    @Test
    void loginRequest_ShouldCreateWithCredentials() {
        // Act
        LoginRequest request = new LoginRequest("user@example.com", "password");

        // Assert
        assertEquals("user@example.com", request.email());
        assertEquals("password", request.password());
    }

    @Test
    void authResponse_ShouldCreateWithTokenAndUserInfo() {
        // Act
        AuthResponse response = new AuthResponse("jwt-token", "user@example.com", "ROLE_PLAYER", 1L);

        // Assert
        assertEquals("jwt-token", response.token());
        assertEquals("user@example.com", response.email());
        assertEquals("ROLE_PLAYER", response.role());
        assertEquals(1L, response.profileId());
    }

    @Test
    void depositRequest_ShouldCreateWithAmount() {
        // Act
        DepositRequest request = new DepositRequest(100.0);

        // Assert
        assertEquals(100.0, request.amount());
    }

    @Test
    void transferRequest_ShouldCreateWithUserIdAndAmount() {
        // Act
        TransferRequest request = new TransferRequest(2L, 50.0);

        // Assert
        assertEquals(2L, request.toUserId());
        assertEquals(50.0, request.amount());
    }

    @Test
    void medicalRecordDTO_ShouldCreateWithAllFields() {
        // Act
        MedicalRecordDTO record = new MedicalRecordDTO(
                1L,
                75.5,
                180.0,
                "O+",
                "None",
                "Peanuts",
                java.time.LocalDate.of(2024, 1, 15),
                1L,
                1L
        );

        // Assert
        assertEquals(1L, record.id());
        assertEquals(75.5, record.weight());
        assertEquals(180.0, record.height());
        assertEquals("O+", record.bloodType());
        assertEquals("None", record.chronicDiseases());
        assertEquals("Peanuts", record.allergies());
        assertEquals(1L, record.playerProfileId());
        assertEquals(1L, record.healthProfessionalId());
    }

    @Test
    void injuryDTO_ShouldCreateWithAllFields() {
        // Act
        InjuryDTO injury = new InjuryDTO(
                1L,
                "Knee injury during match",
                "Rest for 2 weeks",
                java.time.LocalDate.of(2024, 1, 15),
                tn.esprit.pi.domain.InjurySeverity.MODERATE,
                1L
        );

        // Assert
        assertEquals(1L, injury.id());
        assertEquals("Knee injury during match", injury.report());
        assertEquals("Rest for 2 weeks", injury.recommendation());
        assertEquals(tn.esprit.pi.domain.InjurySeverity.MODERATE, injury.severity());
        assertEquals(1L, injury.medicalRecordId());
    }

    @Test
    void recommendationRequest_ShouldCreateWithRecommendation() {
        // Act
        RecommendationRequest request = new RecommendationRequest("Take rest for 1 week");

        // Assert
        assertEquals("Take rest for 1 week", request.recommendation());
    }

    @Test
    void walletAdminDTO_ShouldCreateWithAdminInfo() {
        // Act
        WalletAdminDTO adminWallet = new WalletAdminDTO(
                1L,
                100,
                1L,
                "John Doe",
                "john@example.com",
                "PLAYER",
                "123456789"
        );

        // Assert
        assertEquals(1L, adminWallet.id());
        assertEquals(100, adminWallet.points());
        assertEquals(1L, adminWallet.userId());
        assertEquals("John Doe", adminWallet.username());
        assertEquals("PLAYER", adminWallet.role());
        assertEquals("123456789", adminWallet.phone());
    }
}