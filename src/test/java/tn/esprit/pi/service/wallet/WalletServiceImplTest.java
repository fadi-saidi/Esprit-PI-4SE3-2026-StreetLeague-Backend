package tn.esprit.pi.service.wallet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.dto.TransactionResponse;
import tn.esprit.pi.repository.TransactionRepository;
import tn.esprit.pi.repository.UserRepository;
import tn.esprit.pi.repository.WalletRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - WalletServiceImpl")
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
        user.setEmail("test@esprit.tn");
        user.setUsername("testuser");

        // Wallet avec 500 points (utilisé quand le wallet existe)
        wallet = Wallet.builder()
                .id(10L)
                .points(500)
                .user(user)
                .build();

        transaction = Transaction.builder()
                .id(100L)
                .user(user)
                .amount(100.0)
                .earnedPoints(10)
                .type(TransactionType.DEPOSIT)
                .description("Test deposit")
                .date(LocalDateTime.now())
                .build();
    }

    // ====================== getOrCreateWallet ======================

    @Test
    @DisplayName("Devrait retourner le wallet existant")
    void getOrCreateWallet_WhenExists() {
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

        Wallet result = walletService.getOrCreateWallet(user);

        assertThat(result.getPoints()).isEqualTo(500);
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Devrait créer un nouveau wallet avec 0 points si inexistant")
    void getOrCreateWallet_WhenNotExists() {
        when(walletRepository.findByUser(user)).thenReturn(Optional.empty());

        // Mock du save pour retourner un wallet avec 0 points
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> {
            Wallet newWallet = invocation.getArgument(0);
            // S'assurer que points = 0
            if (newWallet.getPoints() != 0) {
                newWallet.setPoints(0);
            }
            return newWallet;
        });

        Wallet result = walletService.getOrCreateWallet(user);

        assertThat(result).isNotNull();
        assertThat(result.getPoints()).isEqualTo(0);
        verify(walletRepository).save(any(Wallet.class));
    }

    // ====================== deposit ======================

    @Test
    @DisplayName("Devrait déposer des points avec succès")
    void deposit_Success() {
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> {
            Wallet w = inv.getArgument(0);
            return w;
        });
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Wallet result = walletService.deposit(user, 150.0);

        assertThat(result.getPoints()).isEqualTo(515); // 500 + 15 (150/10)
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Devrait lever exception si montant <= 0 pour deposit")
    void deposit_InvalidAmount_ThrowsException() {
        assertThatThrownBy(() -> walletService.deposit(user, 0.0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ====================== withdraw ======================

    @Test
    @DisplayName("Devrait retirer des points avec succès")
    void withdraw_Success() {
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Wallet result = walletService.withdraw(user, 200.0);

        assertThat(result.getPoints()).isEqualTo(480); // 500 - 20
    }

    @Test
    @DisplayName("Devrait lever exception si solde insuffisant")
    void withdraw_InsufficientBalance_ThrowsException() {
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.withdraw(user, 6000.0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ====================== getHistory ======================

    @Test
    @DisplayName("Devrait retourner l'historique des transactions")
    void getHistory_Success() {
        // Important : associer le wallet à l'utilisateur pour éviter NPE
        user.setWallet(wallet);   // ← Correction principale

        when(transactionRepository.findByUser(user)).thenReturn(List.of(transaction));

        List<TransactionResponse> history = walletService.getHistory(user);

        assertThat(history).isNotEmpty();
        assertThat(history.get(0).getTransactionType()).isNotNull();
    }

    // ====================== Admin methods ======================

    @Test
    @DisplayName("Devrait mettre à jour les points d'un wallet")
    void updatePoints_Success() {
        when(walletRepository.findById(10L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

        Wallet result = walletService.updatePoints(10L, 999);

        assertThat(result.getPoints()).isEqualTo(999);
    }
    @Test
    @DisplayName("Transfert - Succès entre deux utilisateurs")
    void transfer_Success() {
        User recipient = new User();
        recipient.setId(2L);
        Wallet recipientWallet = Wallet.builder().id(11L).points(100).build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet)); // from 500
        when(walletRepository.findByUser(recipient)).thenReturn(Optional.of(recipientWallet)); // to 100

        walletService.transfer(user, 2L, 100.0); // 100/10 = 10 points

        assertThat(wallet.getPoints()).isEqualTo(490);
        assertThat(recipientWallet.getPoints()).isEqualTo(110);
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Transfert - Échec si solde insuffisant")
    void transfer_Insufficient_ThrowsException() {
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet)); // 500 pts
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> walletService.transfer(user, 2L, 6000.0))
                .isInstanceOf(IllegalStateException.class);
    }
}