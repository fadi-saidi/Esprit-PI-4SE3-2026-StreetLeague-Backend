package tn.esprit.pi.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.domain.Wallet;
import tn.esprit.pi.domain.Role;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class WalletRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WalletRepository walletRepository;

    @Test
    void findByUser_ShouldReturnWallet() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("Test User");
        user.setRole(Role.PLAYER);
        user.setPassword("password");
        user.setEnabled(true);
        entityManager.persist(user);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setPoints(100);
        entityManager.persist(wallet);
        entityManager.flush();

        // When
        Optional<Wallet> found = walletRepository.findByUser(user);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getPoints()).isEqualTo(100);
    }

    @Test
    void findByUser_ShouldReturnEmptyWhenNotFound() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("Test User");
        user.setRole(Role.PLAYER);
        user.setPassword("password");
        user.setEnabled(true);
        entityManager.persist(user);
        entityManager.flush();

        // When
        Optional<Wallet> found = walletRepository.findByUser(user);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void save_ShouldPersistWallet() {
        // Given
        User user = new User();
        user.setEmail("save@example.com");
        user.setUsername("Save User");
        user.setRole(Role.PLAYER);
        user.setPassword("password");
        user.setEnabled(true);
        entityManager.persist(user);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setPoints(50);

        // When
        Wallet saved = walletRepository.save(wallet);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPoints()).isEqualTo(50);
    }
}
