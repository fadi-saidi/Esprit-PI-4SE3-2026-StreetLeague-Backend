package tn.esprit.pi.service.wallet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.TransactionResponse;
import tn.esprit.pi.repository.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    private User user;
    private Wallet wallet;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("Test User");
        user.setRole(Role.PLAYER);

        wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUser(user);
        wallet.setPoints(50);

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setUser(user);
        transaction.setAmount(25.0);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setDescription("Test transaction");
        transaction.setDate(LocalDateTime.now());
        transaction.setEarnedPoints(2);
    }

    @Test
    void getOrCreateWallet_ShouldReturnExistingWallet() {
        // Arrange
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

        // Act
        Wallet result = walletService.getOrCreateWallet(user);

        // Assert
        assertNotNull(result);
        assertEquals(wallet.getId(), result.getId());
        assertEquals(50, result.getPoints());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void getOrCreateWallet_ShouldCreateNewWallet() {
        // Arrange
        when(walletRepository.findByUser(user)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // Act
        Wallet result = walletService.getOrCreateWallet(user);

        // Assert
        assertNotNull(result);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void getWallet_ShouldReturnWallet() {
        // Arrange
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

        // Act
        Wallet result = walletService.getWallet(user);

        // Assert
        assertNotNull(result);
        assertEquals(wallet.getId(), result.getId());
    }

    @Test
    void getWallet_ShouldThrowExceptionWhenNotFound() {
        // Arrange
        when(walletRepository.findByUser(user)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> walletService.getWallet(user));
        assertTrue(exception.getMessage().contains("Wallet not found"));
    }

    @Test
    void deposit_ShouldIncreasePointsAndCreateTransaction() {
        // Arrange
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        Wallet result = walletService.deposit(user, 100.0);

        // Assert
        assertNotNull(result);
        verify(walletRepository).save(any(Wallet.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void deposit_ShouldThrowExceptionForNegativeAmount() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> walletService.deposit(user, -10.0));
        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    void withdraw_ShouldDecreasePointsAndCreateTransaction() {
        // Arrange
        wallet.setPoints(100); // Enough points for withdrawal
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        Wallet result = walletService.withdraw(user, 50.0);

        // Assert
        assertNotNull(result);
        verify(walletRepository).save(any(Wallet.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void withdraw_ShouldThrowExceptionForInsufficientPoints() {
        // Arrange
        wallet.setPoints(1); // Not enough points
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> walletService.withdraw(user, 100.0));
        assertEquals("Insufficient points", exception.getMessage());
    }

    @Test
    void transfer_ShouldTransferPointsBetweenUsers() {
        // Arrange
        User toUser = new User();
        toUser.setId(2L);
        toUser.setEmail("recipient@test.com");
        
        Wallet toWallet = new Wallet();
        toWallet.setId(2L);
        toWallet.setUser(toUser);
        toWallet.setPoints(0);
        
        wallet.setPoints(100); // Enough points for transfer
        
        when(userRepository.findById(2L)).thenReturn(Optional.of(toUser));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(walletRepository.findByUser(toUser)).thenReturn(Optional.of(toWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        walletService.transfer(user, 2L, 50.0);

        // Assert
        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void getHistory_ShouldReturnTransactionList() {
        // Arrange
        user.setWallet(wallet); // Set wallet for user
        List<Transaction> transactions = Arrays.asList(transaction);
        when(transactionRepository.findByUser(user)).thenReturn(transactions);

        // Act
        List<TransactionResponse> result = walletService.getHistory(user);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(transaction.getId(), result.get(0).getId());
    }

    @Test
    void getAllWallets_ShouldReturnAllWallets() {
        // Arrange
        List<Wallet> wallets = Arrays.asList(wallet);
        when(walletRepository.findAll()).thenReturn(wallets);

        // Act
        List<Wallet> result = walletService.getAllWallets();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(wallet.getId(), result.get(0).getId());
    }

    @Test
    void updatePoints_ShouldUpdateWalletPoints() {
        // Arrange
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // Act
        Wallet result = walletService.updatePoints(1L, 100);

        // Assert
        assertNotNull(result);
        verify(walletRepository).save(any(Wallet.class));
    }
}