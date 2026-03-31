package tn.esprit.pi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.domain.Transaction;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.domain.Wallet;
import tn.esprit.pi.repository.TransactionRepository;
import tn.esprit.pi.repository.WalletRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private tn.esprit.pi.service.wallet.WalletServiceImpl walletService;

    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        wallet = Wallet.builder()
                .user(user)
                .points(100)
                .build();
    }

    @Test
    void deposit_ShouldAddPointsToWallet() {
        // Arrange
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        // Act
        Wallet result = walletService.deposit(user, 50.0);

        // Assert
        assert result.getPoints() == 150;
        verify(walletRepository).save(wallet);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void withdraw_ShouldDeductPointsFromWallet() {
        // Arrange
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        // Act
        Wallet result = walletService.withdraw(user, 50.0);

        // Assert
        assert result.getPoints() == 50;
        verify(walletRepository).save(wallet);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void getWallet_ShouldReturnWallet() {
        // Arrange
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

        // Act
        Wallet result = walletService.getWallet(user);

        // Assert
        assert result.getPoints() == 100;
    }

    @Test
    void getOrCreateWallet_ShouldReturnExistingWallet() {
        // Arrange
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

        // Act
        Wallet result = walletService.getOrCreateWallet(user);

        // Assert
        assert result.getPoints() == 100;
    }

    @Test
    void getOrCreateWallet_ShouldCreateNewWallet() {
        // Arrange
        when(walletRepository.findByUser(user)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // Act
        Wallet result = walletService.getOrCreateWallet(user);

        // Assert
        assert result.getPoints() == 100;
        verify(walletRepository).save(any(Wallet.class));
    }
}
