package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.domain.Transaction;
import tn.esprit.pi.domain.TransactionType;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.domain.Wallet;
import tn.esprit.pi.repository.TransactionRepository;
import tn.esprit.pi.repository.UserRepository;
import tn.esprit.pi.repository.WalletRepository;
import tn.esprit.pi.service.wallet.IWalletService;
import tn.esprit.pi.dto.Dtos;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("*")
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final IWalletService walletService;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // Constants pour la cohérence des réponses JSON
    private static final String AMOUNT_KEY = "amount";
    private static final String MESSAGE_KEY = "message";
    private static final String TRANSACTION_ID_KEY = "transactionId";
    private static final String ERROR_KEY = "error";

    // --- DTOs INTERNES AVEC VALIDATION ---

    public record DepositRequest(
            @NotNull(message = "Amount is required")
            @Positive(message = "Amount must be strictly positive")
            Double amount,
            String description
    ) {}

    public record TransferRequest(
            @NotNull(message = "Recipient user ID is required")
            Long recipientId,
            @NotNull(message = "Amount is required")
            @Positive(message = "Amount must be strictly positive")
            Double amount,
            String description
    ) {}

    public record WithdrawRequest(
            @NotNull(message = "Amount is required")
            @Positive(message = "Amount must be strictly positive")
            Double amount,
            String description
    ) {}

    // --- ENDPOINTS UTILISATEUR ---

    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getWalletBalance(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Wallet wallet = walletService.getOrCreateWallet(user);
        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("points", wallet.getPoints());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/deposit")
    public ResponseEntity<Map<String, Object>> deposit(@Valid @RequestBody DepositRequest request,
                                                       Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Wallet wallet = walletService.getOrCreateWallet(user);

        // Logique métier : 1 DT = 1 Point (ajustable)
        int pointsToAdd = (int) Math.round(request.amount());
        wallet.setPoints(wallet.getPoints() + pointsToAdd);
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .user(user)
                .amount(request.amount())
                .earnedPoints(pointsToAdd)
                .type(TransactionType.DEPOSIT)
                .description(request.description() != null ? request.description() : "Manual deposit")
                .date(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);

        return ResponseEntity.ok(Map.of(
                MESSAGE_KEY, "Deposit successful",
                AMOUNT_KEY, request.amount(),
                "pointsAdded", pointsToAdd,
                "newBalance", wallet.getPoints(),
                TRANSACTION_ID_KEY, transaction.getId()
        ));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Map<String, Object>> transfer(@Valid @RequestBody TransferRequest request,
                                                        Authentication auth) {
        User sender = userRepository.findByEmail(auth.getName()).orElse(null);
        if (sender == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        // 1. Empêcher l'auto-transfert
        if (sender.getId().equals(request.recipientId())) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "You cannot transfer points to yourself"));
        }

        // 2. Vérifier destinataire
        User recipient = userRepository.findById(request.recipientId()).orElse(null);
        if (recipient == null) return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "Recipient not found"));

        Wallet senderWallet = walletService.getOrCreateWallet(sender);
        int pointsToTransfer = (int) Math.round(request.amount());

        // 3. Vérifier solde
        if (senderWallet.getPoints() < pointsToTransfer) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "Insufficient balance. Available: " + senderWallet.getPoints()));
        }

        Wallet recipientWallet = walletService.getOrCreateWallet(recipient);

        // 4. Exécuter transfert
        senderWallet.setPoints(senderWallet.getPoints() - pointsToTransfer);
        recipientWallet.setPoints(recipientWallet.getPoints() + pointsToTransfer);
        walletRepository.save(senderWallet);
        walletRepository.save(recipientWallet);

        // 5. Historique
        Transaction senderTx = Transaction.builder()
                .user(sender)
                .amount(-request.amount())
                .earnedPoints(-pointsToTransfer)
                .type(TransactionType.TRANSFER)
                .description("Sent to " + recipient.getUsername())
                .date(LocalDateTime.now())
                .build();
        transactionRepository.save(senderTx);

        Transaction recipientTx = Transaction.builder()
                .user(recipient)
                .amount(request.amount())
                .earnedPoints(pointsToTransfer)
                .type(TransactionType.TRANSFER)
                .description("Received from " + sender.getUsername())
                .date(LocalDateTime.now())
                .build();
        transactionRepository.save(recipientTx);

        return ResponseEntity.ok(Map.of(MESSAGE_KEY, "Transfer successful", "newBalance", senderWallet.getPoints()));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Map<String, Object>> withdraw(@Valid @RequestBody WithdrawRequest request,
                                                        Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Wallet wallet = walletService.getOrCreateWallet(user);
        int pointsToWithdraw = (int) Math.round(request.amount());

        if (wallet.getPoints() < pointsToWithdraw) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "Insufficient balance"));
        }

        wallet.setPoints(wallet.getPoints() - pointsToWithdraw);
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .user(user)
                .amount(-request.amount())
                .earnedPoints(-pointsToWithdraw)
                .type(TransactionType.WITHDRAWAL)
                .description(request.description() != null ? request.description() : "Manual withdrawal")
                .date(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);

        return ResponseEntity.ok(Map.of(MESSAGE_KEY, "Withdrawal successful", "newBalance", wallet.getPoints()));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Map<String, Object>>> getTransactionHistory(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<Transaction> transactions = transactionRepository.findByUser(user);
        return ResponseEntity.ok(transactions.stream().map(t -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getId());
            m.put("amount", t.getAmount());
            m.put("type", t.getType());
            m.put("date", t.getDate());
            m.put("description", t.getDescription());
            return m;
        }).toList());
    }

    // --- ADMIN SECTION ---

    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<Dtos.WalletAdminDTO>> getAllWallets() {
        return ResponseEntity.ok(walletService.getAllWalletsAdmin());
    }

    @PutMapping("/admin/{id}/points")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Wallet> updatePoints(@PathVariable Long id, @RequestParam int points) {
        return ResponseEntity.ok(walletService.updatePoints(id, points));
    }
}