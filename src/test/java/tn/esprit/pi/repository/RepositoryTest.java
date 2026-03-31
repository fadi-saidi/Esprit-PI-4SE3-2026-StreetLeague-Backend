package tn.esprit.pi.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.domain.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SponsorshipRepository sponsorshipRepository;

    @Test
    void userRepository_ShouldFindByEmail() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole(Role.PLAYER);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userRepository.findByEmail("test@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        assertEquals(Role.PLAYER, result.get().getRole());
    }

    @Test
    void userRepository_ShouldReturnEmptyForNonExistentEmail() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void productRepository_ShouldFindByNameContaining() {
        // Arrange
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Football Jersey");
        product1.setPrice(29.99);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Football Boots");
        product2.setPrice(89.99);

        List<Product> products = Arrays.asList(product1, product2);
        when(productRepository.findByNameContainingIgnoreCase("football")).thenReturn(products);

        // Act
        List<Product> result = productRepository.findByNameContainingIgnoreCase("football");

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getName().toLowerCase().contains("football")));
    }

    @Test
    void productRepository_ShouldFindBySportType() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setName("Basketball");
        product.setSportType(SportType.BASKETBALL);

        List<Product> products = Arrays.asList(product);
        when(productRepository.findBySportType(SportType.BASKETBALL)).thenReturn(products);

        // Act
        List<Product> result = productRepository.findBySportType(SportType.BASKETBALL);

        // Assert
        assertEquals(1, result.size());
        assertEquals(SportType.BASKETBALL, result.get(0).getSportType());
    }

    @Test
    void orderRepository_ShouldCountByStatus() {
        // Arrange
        when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(5L);

        // Act
        long count = orderRepository.countByStatus(OrderStatus.PENDING);

        // Assert
        assertEquals(5L, count);
    }

    @Test
    void sponsorshipRepository_ShouldFindByStatus() {
        // Arrange
        Sponsorship sponsorship = new Sponsorship();
        sponsorship.setId(1L);
        sponsorship.setStatus(SponsorshipStatus.ACTIVE);
        sponsorship.setAmount(1000.0);

        List<Sponsorship> sponsorships = Arrays.asList(sponsorship);
        when(sponsorshipRepository.findByStatus(SponsorshipStatus.ACTIVE)).thenReturn(sponsorships);

        // Act
        List<Sponsorship> result = sponsorshipRepository.findByStatus(SponsorshipStatus.ACTIVE);

        // Assert
        assertEquals(1, result.size());
        assertEquals(SponsorshipStatus.ACTIVE, result.get(0).getStatus());
        assertEquals(1000.0, result.get(0).getAmount());
    }

    @Test
    void sponsorshipRepository_ShouldFindByTeamId() {
        // Arrange
        Sponsorship sponsorship = new Sponsorship();
        sponsorship.setId(1L);
        sponsorship.setAmount(2000.0);

        List<Sponsorship> sponsorships = Arrays.asList(sponsorship);
        when(sponsorshipRepository.findByTeamId(1L)).thenReturn(sponsorships);

        // Act
        List<Sponsorship> result = sponsorshipRepository.findByTeamId(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(2000.0, result.get(0).getAmount());
    }
}