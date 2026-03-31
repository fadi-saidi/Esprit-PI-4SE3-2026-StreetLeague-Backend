package tn.esprit.pi.dto;

import jakarta.validation.constraints.*;
import tn.esprit.pi.domain.InjurySeverity;
import tn.esprit.pi.domain.Role;
import java.time.LocalDate;

public class Dtos {

    // --- AUTH DTOs (Ceux qui manquaient) ---

    public record RegisterRequest(
            @NotBlank(message = "Full name is required") String fullName,
            @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
            @NotBlank(message = "Password is required") @Size(min = 6, message = "Password min 6 chars") String password,
            @NotNull(message = "Role is required") Role role,
            String dateOfBirth,
            String certificate,
            String licenseNumber,
            String specialty,
            Integer experienceYears,
            String companyName,
            String logo,
            String contactEmail,
            Double budget,
            String phone
    ) {}

    public record LoginRequest(
            @NotBlank(message = "Email is required") @Email String email,
            @NotBlank(message = "Password is required") String password
    ) {}

    public record AuthResponse(
            String token,
            String email,
            String role,
            Long profileId
    ) {}

    // --- WALLET DTOs ---

    public record DepositRequest(
            @NotNull @Positive(message = "Amount must be greater than 0") Double amount,
            String description
    ) {}

    public record TransferRequest(
            @NotNull(message = "Recipient ID is required") Long recipientId,
            @NotNull @Positive(message = "Amount must be greater than 0") Double amount,
            String description
    ) {}

    public record WithdrawRequest(
            @NotNull @Positive(message = "Amount must be greater than 0") Double amount,
            String description
    ) {}

    // --- HEALTH DTOs ---

    public record MedicalRecordDTO(
            Long id,
            @NotNull @DecimalMin(value = "20.0") @DecimalMax(value = "300.0") Double weight,
            @NotNull @DecimalMin(value = "50.0") @DecimalMax(value = "250.0") Double height,
            @NotBlank String bloodType,
            String chronicDiseases,
            String allergies,
            @PastOrPresent LocalDate lastCheckup,
            @NotNull Long playerProfileId,
            Long healthProfessionalId
    ) {}

    public record InjuryDTO(
            Long id,
            @NotBlank String report,
            String recommendation,
            @NotNull @PastOrPresent LocalDate date,
            @NotNull InjurySeverity severity,
            @NotNull Long medicalRecordId
    ) {}

    public record RecommendationRequest(
            @NotBlank String recommendation
    ) {}

    public record WalletAdminDTO(
            Long id, int points, Long userId, String username, String email, String role, String phone
    ) {}
}