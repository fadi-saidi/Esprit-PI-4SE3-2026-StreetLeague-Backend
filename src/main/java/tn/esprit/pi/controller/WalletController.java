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
import tn.esprit.pi.service.WalletService;

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

    private final WalletService walletService;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getWalletBalance(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Wallet wallet = walletRepository.findByUser(user).orElse(null);
        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("points", wallet != null ? wallet.getPoints() : 0);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactionHistory(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        List<Transaction> transactions = transactionRepository.findByUser(user);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getWalletStats(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        List<Transaction> transactions = transactionRepository.findByUser(user);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTransactions", transactions.size());
        stats.put("totalPointsEarned", transactions.stream().mapToInt(Transaction::getEarnedPoints).sum());
        stats.put("totalSpent", transactions.stream()
                .filter(t -> t.getAmount() < 0)
                .mapToDouble(t -> Math.abs(t.getAmount()))
                .sum());
        stats.put("totalEarned", transactions.stream()
                .filter(t -> t.getAmount() > 0)
                .mapToDouble(Transaction::getAmount)
                .sum());
        stats.put("currentBalance", walletService.getWalletPoints(user));
        
        return ResponseEntity.ok(stats);
    }

    // DTO for deposit request
    public record DepositRequest(
            @NotNull(message = "Amount is required")
            @Positive(message = "Amount must be positive")
            Double amount,
            
            String description
    ) {}

    // DTO for transfer request
    public record TransferRequest(
            @NotNull(message = "Recipient user ID is required")
            Long recipientId,
            
            @NotNull(message = "Amount is required")
            @Positive(message = "Amount must be positive")
            Double amount,
            
            String description
    ) {}

    @PostMapping("/deposit")
    public ResponseEntity<Map<String, Object>> deposit(@Valid @RequestBody DepositRequest request, 
                                                       Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Get or create wallet
        Wallet wallet = walletRepository.findByUser(user)
                .orElseGet(() -> {
                    Wallet newWallet = Wallet.builder()
                            .user(user)
                            .points(0)
                            .build();
                    return walletRepository.save(newWallet);
                });

        // Convert amount to points (1:1 ratio)
        int pointsToAdd = (int) Math.round(request.amount());
        wallet.setPoints(wallet.getPoints() + pointsToAdd);
        walletRepository.save(wallet);

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .user(user)
                .amount(request.amount())
                .earnedPoints(pointsToAdd)
                .type(TransactionType.DEPOSIT)
                .description(request.description() != null ? request.description() : "Manual deposit")
                .date(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);

        // Return response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Deposit successful");
        response.put("amount", request.amount());
        response.put("pointsAdded", pointsToAdd);
        response.put("newBalance", wallet.getPoints());
        response.put("transactionId", transaction.getId());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Map<String, Object>> transfer(@Valid @RequestBody TransferRequest request, 
                                                        Authentication auth) {
        User sender = userRepository.findByEmail(auth.getName()).orElse(null);
        if (sender == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Find recipient user
        User recipient = userRepository.findById(request.recipientId()).orElse(null);
        if (recipient == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Recipient user not found"));
        }

        // Check if trying to transfer to self
        if (sender.getId().equals(recipient.getId())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Cannot transfer to yourself"));
        }

        // Get sender's wallet
        Wallet senderWallet = walletRepository.findByUser(sender)
                .orElseGet(() -> {
                    Wallet newWallet = Wallet.builder()
                            .user(sender)
                            .points(0)
                            .build();
                    return walletRepository.save(newWallet);
                });

        // Convert amount to points (1:1 ratio)
        int pointsToTransfer = (int) Math.round(request.amount());

        // Check if sender has sufficient balance
        if (senderWallet.getPoints() < pointsToTransfer) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Insufficient balance. Available: " + senderWallet.getPoints() + " points, Required: " + pointsToTransfer + " points"));
        }

        // Get or create recipient's wallet
        Wallet recipientWallet = walletRepository.findByUser(recipient)
                .orElseGet(() -> {
                    Wallet newWallet = Wallet.builder()
                            .user(recipient)
                            .points(0)
                            .build();
                    return walletRepository.save(newWallet);
                });

        // Perform transfer
        senderWallet.setPoints(senderWallet.getPoints() - pointsToTransfer);
        recipientWallet.setPoints(recipientWallet.getPoints() + pointsToTransfer);
        
        walletRepository.save(senderWallet);
        walletRepository.save(recipientWallet);

        // Create transaction records
        String transferDescription = request.description() != null ? request.description() : "Transfer to " + recipient.getUsername();
        String receiveDescription = "Transfer from " + sender.getUsername();

        // Sender transaction (withdrawal)
        Transaction senderTransaction = Transaction.builder()
                .user(sender)
                .amount(-request.amount())
                .earnedPoints(-pointsToTransfer)
                .type(TransactionType.WITHDRAWAL)
                .description(transferDescription)
                .date(LocalDateTime.now())
                .build();
        transactionRepository.save(senderTransaction);

        // Recipient transaction (deposit)
        Transaction recipientTransaction = Transaction.builder()
                .user(recipient)
                .amount(request.amount())
                .earnedPoints(pointsToTransfer)
                .type(TransactionType.DEPOSIT)
                .description(receiveDescription)
                .date(LocalDateTime.now())
                .build();
        transactionRepository.save(recipientTransaction);

        // Return response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Transfer successful");
        response.put("amount", request.amount());
        response.put("pointsTransferred", pointsToTransfer);
        response.put("recipientUsername", recipient.getUsername());
        response.put("senderNewBalance", senderWallet.getPoints());
        response.put("transactionId", senderTransaction.getId());
        
        return ResponseEntity.ok(response);
    }
}