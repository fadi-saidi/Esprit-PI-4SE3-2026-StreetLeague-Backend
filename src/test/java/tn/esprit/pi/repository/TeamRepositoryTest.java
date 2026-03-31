package tn.esprit.pi.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import tn.esprit.pi.domain.Team;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TeamRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TeamRepository teamRepository;

    @Test
    void save_ShouldPersistTeam() {
        // Given
        Team team = new Team();
        team.setName("Test Team");

        // When
        Team saved = teamRepository.save(team);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Team");
    }

    @Test
    void findById_ShouldReturnTeam() {
        // Given
        Team team = new Team();
        team.setName("Test Team");
        Team saved = entityManager.persistAndFlush(team);

        // When
        Optional<Team> found = teamRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Team");
    }
}
