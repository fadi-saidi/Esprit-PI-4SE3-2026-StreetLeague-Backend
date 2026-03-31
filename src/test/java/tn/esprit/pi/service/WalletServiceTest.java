package tn.esprit.pi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.domain.Wallet;
import tn.esprit.pi.repository.TransactionRepository;
import tn.esprit.pi.repository.WalletRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock private WalletRepository walletRepository;
    @Mock private TransactionRepository transactionRepository;
    @InjectMocks private WalletService walletService;

    @Test
    void processOrderPayment_InsufficientPoints_ThrowsException() {
        User user = new User();
        Wallet wallet = Wallet.builder().points(10).build();
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

        assertThrows(RuntimeException.class, () -> walletService.processOrderPayment(user, 100.0, 1L));
    }

    @Test
    void processOrderPayment_Success_UpdatesPoints() {
        User user = new User();
        Wallet wallet = Wallet.builder().points(200).build();
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

        // 100€ -> déduit 100 pts + gagne 1 pt (1%) = 101 pts restants
        walletService.processOrderPayment(user, 100.0, 1L);

        assertEquals(101, wallet.getPoints());
        verify(walletRepository).save(wallet);
        verify(transactionRepository).save(any());
    }

    @Test
    void processOrderRefund_Success() {
        User user = new User();
        Wallet wallet = Wallet.builder().points(50).build();
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

        walletService.processOrderRefund(user, 50.0, 1L);

        assertEquals(100, wallet.getPoints());
    }
}