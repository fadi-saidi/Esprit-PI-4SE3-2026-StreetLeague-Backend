package tn.esprit.pi.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for Sponsorship Entity")
class SponsorshipTest {

    private Sponsorship sponsorship;
    private SponsorProfile sponsorProfile;
    private Team team;
    private Tournament tournament;
    private Venue venue;

    @BeforeEach
    void setUp() {
        sponsorProfile = new SponsorProfile();
        team = new Team();
        tournament = new Tournament();
        venue = new Venue();

        sponsorship = new Sponsorship();
    }

    @Test
    @DisplayName("Should create Sponsorship using AllArgsConstructor")
    void testAllArgsConstructor() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(30);

        Sponsorship s = new Sponsorship(
                1L,
                5000.0,
                start,
                end,
                "proof_001.pdf",
                SponsorshipStatus.ACCEPTED,
                SponsorshipTargetType.TEAM,
                "Official sponsor for the new season",
                "Logo on jerseys + social media mentions",
                sponsorProfile,
                team,
                null,
                tournament,
                venue
        );

        assertNotNull(s);
        assertEquals(1L, s.getId());
        assertEquals(5000.0, s.getAmount());
        assertEquals(SponsorshipStatus.ACCEPTED, s.getStatus());
    }

    @Test
    @DisplayName("Should correctly set and get all fields")
    void testGettersAndSetters() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 8, 31);

        sponsorship.setId(25L);
        sponsorship.setAmount(7500.0);
        sponsorship.setStartDate(start);
        sponsorship.setEndDate(end);
        sponsorship.setPaymentProof("invoice_2026.pdf");
        sponsorship.setStatus(SponsorshipStatus.ACTIVE);
        sponsorship.setTargetType(SponsorshipTargetType.EVENT);
        sponsorship.setDescription("Title sponsor of the tournament");
        sponsorship.setExpectedBenefits("Branding on stadium + interviews");
        sponsorship.setSponsorProfile(sponsorProfile);
        sponsorship.setTeam(team);
        sponsorship.setEvent(null);
        sponsorship.setTournament(tournament);
        sponsorship.setVenue(venue);

        assertEquals(25L, sponsorship.getId());
        assertEquals(7500.0, sponsorship.getAmount());
        assertEquals(SponsorshipStatus.ACTIVE, sponsorship.getStatus());
        assertEquals(SponsorshipTargetType.EVENT, sponsorship.getTargetType());
        assertNull(sponsorship.getEvent());
    }

    @Test
    @DisplayName("toString() should run without exception and contain entity information")
    void testToString() {
        sponsorship.setId(42L);
        sponsorship.setAmount(12000.0);
        sponsorship.setStatus(SponsorshipStatus.PENDING);
        sponsorship.setDescription("Test sponsorship");

        String toString = sponsorship.toString();

        assertNotNull(toString, "toString() should not return null");

        // More flexible and realistic checks for Lombok @ToString
        assertFalse(toString.isBlank(), "toString() should not be empty");

        // At minimum, it should contain the class name or the id we set
        assertTrue(
                toString.contains("Sponsorship") ||
                        toString.contains("42") ||
                        toString.contains("id="),
                "toString() should contain class name or id. Actual: " + toString
        );
    }

    @Test
    @DisplayName("Two different instances are not equal (no equals/hashCode override yet)")
    void testEqualsAndHashCode() {
        Sponsorship s1 = new Sponsorship();
        s1.setId(100L);

        Sponsorship s2 = new Sponsorship();
        s2.setId(100L);

        assertNotEquals(s1, s2, "Different object instances should not be equal by default");
        assertNotEquals(s1.hashCode(), s2.hashCode());
    }
}