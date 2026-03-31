package tn.esprit.pi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.esprit.pi.domain.Role;
import tn.esprit.pi.domain.Transaction;
import tn.esprit.pi.domain.TransactionType;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.domain.Wallet;
import tn.esprit.pi.repository.TransactionRepository;
import tn.esprit.pi.repository.UserRepository;
import tn.esprit.pi.repository.WalletRepository;
import tn.esprit.pi.service.wallet.IWalletService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private IWalletService walletService;           // ← This was missing!

    @Mock
    private Authentication authentication;

    @InjectMocks
    private WalletController walletController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User user;
    private Wallet wallet;
    private Transaction transaction;

    // Request records (must match controller exactly)
    record DepositRequest(Double amount, String description) {}
    record WithdrawRequest(Double amount, String description) {}
    record TransferRequest(Long recipientId, Double amount, String description) {}

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(walletController).build();
        objectMapper = new ObjectMapper();

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("Test User");
        user.setRole(Role.PLAYER);

        wallet = Wallet.builder()
                .id(1L)
                .user(user)
                .points(100)
                .build();

        transaction = Transaction.builder()
                .id(1L)
                .user(user)
                .amount(50.0)
                .earnedPoints(5)
                .type(TransactionType.DEPOSIT)
                .description("Test deposit")
                .date(LocalDateTime.now())
                .build();
    }

    @Test
    void getWalletBalance_ShouldReturnWalletInfo() throws Exception {
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

        mockMvc.perform(get("/wallet/balance")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("Test User"))
                .andExpect(jsonPath("$.points").value(100));
    }

    @Test
    void getTransactionHistory_ShouldReturnTransactions() throws Exception {
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findByUser(user)).thenReturn(List.of(transaction));

        mockMvc.perform(get("/wallet/transactions")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].amount").value(50.0))
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"));
    }

    @Test
    void getWalletStats_ShouldReturnStatistics() throws Exception {
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByUser(user)).thenReturn(List.of(transaction));
        when(walletService.getOrCreateWallet(user)).thenReturn(wallet);   // ← Fixed NPE

        mockMvc.perform(get("/wallet/stats")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTransactions").value(1))
                .andExpect(jsonPath("$.totalPointsEarned").value(5))
                .andExpect(jsonPath("$.currentBalance").value(100));
    }

    @Test
    void deposit_ShouldAddFundsToWallet() throws Exception {
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction t = i.getArgument(0);
            t.setId(100L);
            return t;
        });

        DepositRequest request = new DepositRequest(100.0, "Test deposit");

        mockMvc.perform(post("/wallet/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Deposit successful"))
                .andExpect(jsonPath("$.newBalance").value(200));
    }

    @Test
    void withdraw_ShouldDeductFundsFromWallet() throws Exception {
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        WithdrawRequest request = new WithdrawRequest(30.0, null);

        mockMvc.perform(post("/wallet/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Withdrawal successful"))
                .andExpect(jsonPath("$.newBalance").value(70));
    }

    @Test
    void transfer_ShouldTransferFundsBetweenUsers() throws Exception {
        User recipient = new User();
        recipient.setId(2L);
        recipient.setUsername("Recipient User");

        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(walletRepository.findByUser(any(User.class))).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        TransferRequest request = new TransferRequest(2L, 25.0, "Gift");

        mockMvc.perform(post("/wallet/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transfer successful"));
    }

    // ==================== Negative Cases ====================

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        when(authentication.getName()).thenReturn("unknown@example.com");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/wallet/balance")
                        .principal(authentication))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnUnauthorizedWhenNoAuthenticationProvided() throws Exception {
        // Do NOT pass .principal() at all → auth will be null
        mockMvc.perform(get("/wallet/balance"))
                .andExpect(status().isUnauthorized());   // This should now pass
    }

    @Test
    void withdraw_ShouldFailWhenInsufficientBalance() throws Exception {
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

        WithdrawRequest request = new WithdrawRequest(150.0, "Too much");

        mockMvc.perform(post("/wallet/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(authentication))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient balance"));
    }

    @Test
    void transfer_ShouldFailWhenRecipientNotFound() throws Exception {
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        TransferRequest request = new TransferRequest(999L, 25.0, "Gift");

        mockMvc.perform(post("/wallet/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(authentication))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recipient not found"));
    }

    @Test
    void transfer_ShouldFailWhenSelfTransfer() throws Exception {
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        TransferRequest request = new TransferRequest(1L, 25.0, "Self gift");

        mockMvc.perform(post("/wallet/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(authentication))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot transfer to yourself"));
    }

    @Test
    void deposit_ShouldFailWhenNegativeAmount() throws Exception {
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        DepositRequest request = new DepositRequest(-50.0, "Negative deposit");

        mockMvc.perform(post("/wallet/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(authentication))
                .andExpect(status().isBadRequest());
    }

    @Test
    void withdraw_ShouldFailWhenNegativeAmount() throws Exception {
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        WithdrawRequest request = new WithdrawRequest(-30.0, "Negative withdraw");

        mockMvc.perform(post("/wallet/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(authentication))
                .andExpect(status().isBadRequest());
    }
}