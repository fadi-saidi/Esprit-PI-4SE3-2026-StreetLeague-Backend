package tn.esprit.pi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.repository.TransactionRepository;
import tn.esprit.pi.repository.UserRepository;
import tn.esprit.pi.repository.WalletRepository;
import tn.esprit.pi.service.wallet.IWalletService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletController - Tests Finalisés")
class WalletControllerTest {

    @Mock private IWalletService walletService;
    @Mock private WalletRepository walletRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;

    @InjectMocks private WalletController walletController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();
    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(walletController).build();

        user = new User();
        user.setId(1L);
        user.setEmail("test@esprit.tn");
        user.setUsername("testuser");

        wallet = Wallet.builder()
                .id(10L)
                .points(1000)
                .user(user)
                .build();

        lenient().when(authentication.getName()).thenReturn("test@esprit.tn");
    }

    @Test
    @DisplayName("GET /wallet/balance - Succès")
    void getBalance_Success() throws Exception {
        when(userRepository.findByEmail("test@esprit.tn")).thenReturn(Optional.of(user));
        when(walletService.getOrCreateWallet(user)).thenReturn(wallet);

        mockMvc.perform(get("/wallet/balance").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points").value(1000));
    }

    @Test
    @DisplayName("POST /wallet/deposit - Succès")
    void deposit_Success() throws Exception {
        WalletController.DepositRequest req = new WalletController.DepositRequest(100.0, "Refill");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(walletService.getOrCreateWallet(user)).thenReturn(wallet);

        // Configuration du Mock pour simuler la génération d'un ID par la DB
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(500L); // On donne un ID manuellement pour éviter le Null sur transaction.getId()
            return t;
        });

        mockMvc.perform(post("/wallet/deposit")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Deposit successful"))
                .andExpect(jsonPath("$.newBalance").value(1100))
                .andExpect(jsonPath("$.transactionId").value(500));
    }

    @Test
    @DisplayName("POST /wallet/transfer - Succès")
    void transfer_Success() throws Exception {
        User recipient = new User();
        recipient.setId(2L);
        recipient.setUsername("dest");

        Wallet recipientWallet = Wallet.builder().points(50).build();
        WalletController.TransferRequest req = new WalletController.TransferRequest(2L, 50.0, "Cadeau");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(walletService.getOrCreateWallet(user)).thenReturn(wallet);
        when(walletService.getOrCreateWallet(recipient)).thenReturn(recipientWallet);

        mockMvc.perform(post("/wallet/transfer")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transfer successful"));

        verify(walletRepository, atLeastOnce()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("POST /wallet/transfer - Erreur auto-transfert")
    void transfer_ToSelf_Error() throws Exception {
        WalletController.TransferRequest req = new WalletController.TransferRequest(1L, 50.0, "Self");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        mockMvc.perform(post("/wallet/transfer")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("You cannot transfer points to yourself"));
    }

    @Test
    @DisplayName("POST /wallet/withdraw - Succès")
    void withdraw_Success() throws Exception {
        WalletController.WithdrawRequest req = new WalletController.WithdrawRequest(100.0, "Withdraw");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(walletService.getOrCreateWallet(user)).thenReturn(wallet);

        mockMvc.perform(post("/wallet/withdraw")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newBalance").value(900));
    }

    @Test
    @DisplayName("POST /wallet/deposit - Utilisateur non trouvé")
    void deposit_UserNotFound() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/wallet/deposit")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":100.0}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /wallet/admin/{id}/points - Admin Success")
    void adminUpdatePoints_Success() throws Exception {
        when(walletService.updatePoints(eq(10L), anyInt())).thenReturn(wallet);

        mockMvc.perform(put("/wallet/admin/10/points").param("points", "500"))
                .andExpect(status().isOk());
    }
}