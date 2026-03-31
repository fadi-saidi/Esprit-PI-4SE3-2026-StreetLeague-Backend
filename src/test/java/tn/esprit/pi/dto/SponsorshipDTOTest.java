package tn.esprit.pi.dto;

import org.junit.jupiter.api.Test;
import tn.esprit.pi.domain.SponsorshipTargetType;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SponsorshipDTOTest {

    @Test
    void createSponsorshipRequest_ResolvesTeamTarget() {
        SponsorshipDTOs.CreateSponsorshipRequest req = new SponsorshipDTOs.CreateSponsorshipRequest(
                1000.0, LocalDate.now(), LocalDate.now().plusMonths(1), "Desc",
                "Benefits", new SponsorshipDTOs.CreateSponsorshipRequest.TargetRef(1L),
                null, null, null, null, "proof.jpg"
        );

        assertEquals(SponsorshipTargetType.TEAM, req.resolvedTargetType());
        assertEquals(1L, req.resolvedTargetId());
    }

    @Test
    void createSponsorshipRequest_ResolvesExpectedBenefitsFromList() {
        List<String> benefitsList = List.of("Logo", "Ads");
        SponsorshipDTOs.CreateSponsorshipRequest req = new SponsorshipDTOs.CreateSponsorshipRequest(
                100.0, LocalDate.now(), LocalDate.now(), "Desc",
                benefitsList, null, null, null, null, null, "url"
        );

        assertEquals("Logo, Ads", req.resolvedExpectedBenefits());
    }
}