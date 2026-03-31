package tn.esprit.pi.dto;

import tn.esprit.pi.domain.InjurySeverity;
import tn.esprit.pi.domain.Role;
import tn.esprit.pi.domain.TransactionType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Dtos {

    public record RegisterRequest(

            String fullName,
            String email,
            String password,
            Role role,
            // Player
            String dateOfBirth,
            // Health Professional, Referee, Coach
            String certificate,
            String licenseNumber,
            String specialty,
            Integer experienceYears,
            // Sponsor
            String companyName,
            String logo,
            String contactEmail,
            Double budget,
            // Venue Owner
            String phone
    ) {}

    public record LoginRequest(
            String email,
            String password
    ) {}

    public record AuthResponse(
            Long id,
            String token,
            String email,
            String role,
            Long profileId
    ) {}



    // Request body for deposit and withdraw: { "amount": 50.0 }
    public record DepositRequest(
            Double amount
    ) {}

    // Request body for transfer: { "toUserId": 7, "amount": 20.0 }
    public record TransferRequest(
            Long toUserId,
            Double amount
    ) {}

    // =========================================================
    //  HEALTH DTOs
    // =========================================================

    public record MedicalRecordDTO(
            Long id,
            Double weight,
            Double height,
            String bloodType,
            String chronicDiseases,
            String allergies,
            LocalDate lastCheckup,
            Long playerProfileId,
            Long healthProfessionalId
    ) {}

    public record InjuryDTO(
            Long id,
            String report,
            String recommendation,
            LocalDate date,
            InjurySeverity severity,
            Long medicalRecordId
    ) {}

    // Request body when doctor adds advice: { "recommendation": "Rest for 2 weeks." }
    public record RecommendationRequest(
            String recommendation
    ) {}
    public record WalletAdminDTO(
            Long id,
            int points,
            Long userId,
            String username,
            String email,
            String role,
            String phone
    ) {}
}
