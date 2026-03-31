package tn.esprit.pi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.repository.PlayerMerchRepository;
import tn.esprit.pi.repository.ProductRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerMerchServiceTest {

    @Mock private PlayerMerchRepository playerMerchRepository;
    @Mock private WalletService walletService;
    @InjectMocks private PlayerMerchService playerMerchService;

    @Test
    void isPlayerMerchandise_ReturnsTrue_WhenExists() {
        Product product = new Product();
        product.setId(1L);

        PlayerMerch pm = new PlayerMerch();
        pm.setProduct(product);

        when(playerMerchRepository.findAll()).thenReturn(List.of(pm));

        assertTrue(playerMerchService.isPlayerMerchandise(product));
    }

    @Test
    void processPlayerMerchSale_ShouldCreditWallet() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Jersey");

        User user = new User();
        PlayerProfile pp = new PlayerProfile();
        pp.setUser(user);

        PlayerMerch pm = new PlayerMerch();
        pm.setProduct(product);
        pm.setSeller(pp);
        pm.setStock(10);

        when(playerMerchRepository.findAll()).thenReturn(List.of(pm));

        playerMerchService.processPlayerMerchSale(product, 2, 100.0);

        verify(walletService).processPlayerMerchSale(eq(user), eq(100.0), anyString());
        assertEquals(8, pm.getStock());
    }
}