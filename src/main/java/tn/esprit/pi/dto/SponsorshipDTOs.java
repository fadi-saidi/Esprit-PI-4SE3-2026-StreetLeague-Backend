package tn.esprit.pi.dto;

import jakarta.validation.constraints.*;
import tn.esprit.pi.domain.SponsorshipTargetType;

import java.time.LocalDate;

public class SponsorshipDTOs {

    public record CreateSponsorshipRequest(
            @NotNull(message = "Amount is required")
            @Positive(message = "Amount must be positive")
            Double amount,

            @NotNull(message = "Start date is required")
            LocalDate startDate,

            @NotNull(message = "End date is required")
            LocalDate endDate,

            @Size(max = 1000, message = "Description must be under 1000 characters")
            String description,

            // accepts both String and String[] from frontend
            Object expectedBenefits,

            // frontend sends { team: {id:1} } or { event: {id:1} } etc.
            TargetRef team,
            TargetRef event,
            TargetRef tournament,
            TargetRef venue,

            // optional explicit targetType override
            SponsorshipTargetType targetType,

            String paymentProof
    ) {
        public record TargetRef(Long id) {}

        // resolve targetType and targetId from whichever field is set
        public SponsorshipTargetType resolvedTargetType() {
            if (targetType != null) return targetType;
            if (team != null) return SponsorshipTargetType.TEAM;
            if (tournament != null) return SponsorshipTargetType.TOURNAMENT;
            if (event != null) return SponsorshipTargetType.EVENT;
            if (venue != null) return SponsorshipTargetType.VENUE;
            return null;
        }

        public Long resolvedTargetId() {
            if (team != null) return team.id();
            if (tournament != null) return tournament.id();
            if (event != null) return event.id();
            if (venue != null) return venue.id();
            return null;
        }

        public String resolvedExpectedBenefits() {
            if (expectedBenefits == null) return null;
            if (expectedBenefits instanceof String s) return s;
            if (expectedBenefits instanceof java.util.List<?> list)
                return String.join(", ", list.stream().map(Object::toString).toList());
            return expectedBenefits.toString();
        }
    }

    public record UpdateSponsorshipRequest(
            @Positive(message = "Amount must be positive")
            Double amount,

            @FutureOrPresent(message = "Start date must be today or in the future")
            LocalDate startDate,

            @Future(message = "End date must be in the future")
            LocalDate endDate,

            @Size(min = 20, max = 1000, message = "Description must be between 20 and 1000 characters")
            String description,

            @Size(min = 10, max = 500, message = "Expected benefits must be between 10 and 500 characters")
            String expectedBenefits
    ) {
    }

    public record SponsorshipResponse(
            Long id,
            Double amount,
            LocalDate startDate,
            LocalDate endDate,
            String paymentProof,
            String status,
            String targetType,
            String description,
            String expectedBenefits,
            Object target
    ) {
    }

    public record AvailableTargetsResponse(
            Long id,
            String name,
            String type,
            String sportType,
            Object extraInfo
    ) {
    }

    public record PaymentProofRequest(
            @NotBlank(message = "Payment proof URL is required")
            @Pattern(regexp = "^https?://.*", message = "Payment proof must be a valid URL")
            String proofUrl
    ) {
    }

    public record RenewSponsorshipRequest(
            @NotNull(message = "Number of months is required")
            @Min(value = 1, message = "Must renew for at least 1 month")
            @Max(value = 60, message = "Cannot renew for more than 60 months at once")
            Integer months
    ) {
    }
}
