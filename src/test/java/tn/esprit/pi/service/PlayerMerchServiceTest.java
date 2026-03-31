package tn.esprit.pi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.repository.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerMerchServiceTest {

    @Mock
    private PlayerMerchRepository playerMerchRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private WalletService walletService;

    @InjectMocks
    private PlayerMerchService playerMerchService;

    private User user;
    private PlayerProfile playerProfile;
    private PlayerMerch playerMerch;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("player@test.com");
        user.setRole(Role.PLAYER);

        playerProfile = new PlayerProfile();
        playerProfile.setId(1L);
        playerProfile.setUser(user);

        product = new Product();
        product.setId(1L);
        product.setName("Custom Jersey");
        product.setPrice(49.99);
        product.setStock(10);

        playerMerch = new PlayerMerch();
        playerMerch.setId(1L);
        playerMerch.setSeller(playerProfile);
        playerMerch.setName("Custom Jersey");
        playerMerch.setPrice(49.99);
        playerMerch.setDescription("Player custom jersey");
        playerMerch.setStatus(MerchStatus.PENDING);
        playerMerch.setStock(10);
        playerMerch.setSubmittedAt(LocalDateTime.now());
        playerMerch.setProduct(product);
    }

    @Test
    void processPlayerMerchSale_ShouldCreditSellerWallet() {
        // Arrange
        List<PlayerMerch> playerMerchList = Arrays.asList(playerMerch);
        when(playerMerchRepository.findAll()).thenReturn(playerMerchList);
        when(playerMerchRepository.save(any(PlayerMerch.class))).thenReturn(playerMerch);

        // Act
        playerMerchService.processPlayerMerchSale(product, 2, 99.98);

        // Assert
        verify(walletService).processPlayerMerchSale(user, 99.98, "Custom Jersey");
        verify(playerMerchRepository).save(playerMerch);
        assertEquals(8, playerMerch.getStock()); // 10 - 2 = 8
    }

    @Test
    void processPlayerMerchSale_ShouldHandleNullStock() {
        // Arrange
        playerMerch.setStock(null);
        List<PlayerMerch> playerMerchList = Arrays.asList(playerMerch);
        when(playerMerchRepository.findAll()).thenReturn(playerMerchList);

        // Act
        playerMerchService.processPlayerMerchSale(product, 2, 99.98);

        // Assert
        verify(walletService).processPlayerMerchSale(user, 99.98, "Custom Jersey");
        verify(playerMerchRepository, never()).save(any(PlayerMerch.class));
    }

    @Test
    void processPlayerMerchSale_ShouldHandleNoMatchingPlayerMerch() {
        // Arrange
        when(playerMerchRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        playerMerchService.processPlayerMerchSale(product, 2, 99.98);

        // Assert
        verify(walletService, never()).processPlayerMerchSale(any(), anyDouble(), anyString());
        verify(playerMerchRepository, never()).save(any(PlayerMerch.class));
    }

    @Test
    void isPlayerMerchandise_ShouldReturnTrueForPlayerMerch() {
        // Arrange
        List<PlayerMerch> playerMerchList = Arrays.asList(playerMerch);
        when(playerMerchRepository.findAll()).thenReturn(playerMerchList);

        // Act
        boolean result = playerMerchService.isPlayerMerchandise(product);

        // Assert
        assertTrue(result);
    }

    @Test
    void isPlayerMerchandise_ShouldReturnFalseForRegularProduct() {
        // Arrange
        when(playerMerchRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        boolean result = playerMerchService.isPlayerMerchandise(product);

        // Assert
        assertFalse(result);
    }

    @Test
    void getPlayerMerchByProduct_ShouldReturnPlayerMerch() {
        // Arrange
        List<PlayerMerch> playerMerchList = Arrays.asList(playerMerch);
        when(playerMerchRepository.findAll()).thenReturn(playerMerchList);

        // Act
        PlayerMerch result = playerMerchService.getPlayerMerchByProduct(product);

        // Assert
        assertNotNull(result);
        assertEquals(playerMerch.getId(), result.getId());
        assertEquals(playerMerch.getName(), result.getName());
    }

    @Test
    void getPlayerMerchByProduct_ShouldReturnNullForNoMatch() {
        // Arrange
        when(playerMerchRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        PlayerMerch result = playerMerchService.getPlayerMerchByProduct(product);

        // Assert
        assertNull(result);
    }

    @Test
    void processPlayerMerchSale_ShouldHandleNullSeller() {
        // Arrange
        playerMerch.setSeller(null);
        List<PlayerMerch> playerMerchList = Arrays.asList(playerMerch);
        when(playerMerchRepository.findAll()).thenReturn(playerMerchList);

        // Act
        playerMerchService.processPlayerMerchSale(product, 2, 99.98);

        // Assert
        verify(walletService, never()).processPlayerMerchSale(any(), anyDouble(), anyString());
        verify(playerMerchRepository, never()).save(any(PlayerMerch.class));
    }

    @Test
    void processPlayerMerchSale_ShouldHandleNullUser() {
        // Arrange
        playerProfile.setUser(null);
        List<PlayerMerch> playerMerchList = Arrays.asList(playerMerch);
        when(playerMerchRepository.findAll()).thenReturn(playerMerchList);

        // Act
        playerMerchService.processPlayerMerchSale(product, 2, 99.98);

        // Assert
        verify(walletService, never()).processPlayerMerchSale(any(), anyDouble(), anyString());
        verify(playerMerchRepository, never()).save(any(PlayerMerch.class));
    }
}