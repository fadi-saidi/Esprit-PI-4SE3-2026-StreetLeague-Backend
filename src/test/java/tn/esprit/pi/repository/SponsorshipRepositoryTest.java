package tn.esprit.pi.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import tn.esprit.pi.domain.*;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SponsorshipRepositoryTest {

    @Autowired
    private SponsorshipRepository sponsorshipRepository;

    @Autowired
    private TestEntityManager entityManager;

    private SponsorProfile sponsorProfile;
    private Team team;
    private Venue venue;

    @BeforeEach
    void setUp() {
        // Create minimal required entities (adjust fields as needed for your entities)
        User user = new User();
        user.setUsername("test_sponsor");
        user.setEmail("sponsor@test.com");
        // Add other mandatory fields (password, roles, etc.) if validation fails
        entityManager.persistAndFlush(user);

        sponsorProfile = new SponsorProfile();
        sponsorProfile.setUser(user);
        sponsorProfile.setCompanyName("Nike Tunisia");
        entityManager.persistAndFlush(sponsorProfile);

        team = new Team();
        team.setName("Club Africain");   // removed manual ID – let DB generate it
        entityManager.persistAndFlush(team);

        venue = new Venue();
        venue.setName("Stade Olympique");
        entityManager.persistAndFlush(venue);

        // Active Sponsorship
        Sponsorship active = new Sponsorship();
        active.setSponsorProfile(sponsorProfile);
        active.setTeam(team);
        active.setStatus(SponsorshipStatus.ACTIVE);
        active.setAmount(5000.0);
        active.setStartDate(LocalDate.of(2025, 1, 1));
        active.setTargetType(SponsorshipTargetType.TEAM);
        entityManager.persistAndFlush(active);

        // Pending Sponsorship
        Sponsorship pending = new Sponsorship();
        pending.setSponsorProfile(sponsorProfile);
        pending.setStatus(SponsorshipStatus.PENDING);
        pending.setAmount(3000.0);
        pending.setStartDate(LocalDate.of(2025, 3, 1));
        pending.setTargetType(SponsorshipTargetType.EVENT);
        entityManager.persistAndFlush(pending);
    }

    @Test
    void shouldFindBySponsorProfile() {
        List<Sponsorship> result = sponsorshipRepository.findBySponsorProfile(sponsorProfile);
        assertThat(result).hasSize(2);
    }

    @Test
    void shouldFindByStatus() {
        List<Sponsorship> activeList = sponsorshipRepository.findByStatus(SponsorshipStatus.ACTIVE);
        assertThat(activeList).hasSize(1);
    }

    @Test
    void shouldFindByTeamId() {
        // Note: After persist, use the generated ID instead of hardcoded 10L if needed
        List<Sponsorship> result = sponsorshipRepository.findByTeamId(team.getId());
        assertThat(result).hasSize(1);
    }
}