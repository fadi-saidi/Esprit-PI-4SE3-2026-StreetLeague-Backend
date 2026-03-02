package tn.esprit.pi.service.wallet;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.Transaction;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.domain.Wallet;
import tn.esprit.pi.dto.TransactionResponse;
import tn.esprit.pi.repository.TransactionRepository;
import tn.esprit.pi.repository.UserRepository;
import tn.esprit.pi.repository.WalletRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements IWalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // ─── Get or create wallet ─────────────────────────────────────────────────

    @Override
    public Wallet getOrCreateWallet(User user) {
        return walletRepository.findByUser(user).orElseGet(() ->
                walletRepository.save(
                        Wallet.builder()
                                .user(user)
                                .points(0)
                                .build()
                )
        );
    }

    // ─── Get wallet (must exist) ──────────────────────────────────────────────

    @Override
    public Wallet getWallet(User user) {
        return walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + user.getEmail()));
    }

    // ─── Deposit → earn points (1 point per 10 units) ────────────────────────

    @Override
    public Wallet deposit(User user, Double amount) {
        if (amount == null || amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        Wallet wallet = getOrCreateWallet(user);
        int earned = (int) (amount / 10);
        wallet.setPoints(wallet.getPoints() + earned);
        walletRepository.save(wallet);

        transactionRepository.save(Transaction.builder()
                .amount(amount)
                .earnedPoints(earned)
                .date(LocalDateTime.now())
                .user(user)
                .build());

        return wallet;
    }

    // ─── Withdraw → spend points ──────────────────────────────────────────────

    @Override
    public Wallet withdraw(User user, Double amount) {
        if (amount == null || amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        Wallet wallet = getWallet(user);
        int pointsNeeded = (int) (amount / 10);

        if (wallet.getPoints() < pointsNeeded)
            throw new IllegalStateException("Insufficient points");

        wallet.setPoints(wallet.getPoints() - pointsNeeded);
        walletRepository.save(wallet);

        transactionRepository.save(Transaction.builder()
                .amount(-amount)
                .earnedPoints(-pointsNeeded)
                .date(LocalDateTime.now())
                .user(user)
                .build());

        return wallet;
    }

    // ─── Transfer points to another user ──────────────────────────────────────

    @Override
    public void transfer(User from, Long toUserId, Double amount) {
        if (amount == null || amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        User to = userRepository.findById(toUserId)
                .orElseThrow(() -> new RuntimeException("Recipient user not found"));

        Wallet fromWallet = getWallet(from);
        int points = (int) (amount / 10);

        if (fromWallet.getPoints() < points)
            throw new IllegalStateException("Insufficient points for transfer");

        // Deduct from sender
        fromWallet.setPoints(fromWallet.getPoints() - points);
        walletRepository.save(fromWallet);

        // Credit to receiver
        Wallet toWallet = getOrCreateWallet(to);
        toWallet.setPoints(toWallet.getPoints() + points);
        walletRepository.save(toWallet);

        LocalDateTime now = LocalDateTime.now();

        // Log both transactions
        transactionRepository.save(Transaction.builder()
                .amount(-amount).earnedPoints(-points).date(now).user(from).build());

        transactionRepository.save(Transaction.builder()
                .amount(amount).earnedPoints(points).date(now).user(to).build());
    }

    // ─── Transaction history ──────────────────────────────────────────────────

    @Override
    public List<TransactionResponse> getHistory(User user) {
        List<Transaction> transactions = transactionRepository.findByUser(user);

        return transactions.stream().map(t -> {
            String type;
            if (t.getAmount() > 0) {
                type = "DEPOSIT";
            } else if (t.getAmount() < 0) {
                type = "WITHDRAWAL";
            } else {
                type = "TRANSFER";
            }

            return TransactionResponse.builder()
                    .id(t.getId())
                    .amount(t.getAmount())
                    .earnedPoints(t.getEarnedPoints())
                    .date(t.getDate())
                    .transactionType(type)
                    .userId((long) user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .currentPoints(user.getWallet().getPoints())
                    .build();
        }).collect(Collectors.toList());
    }

    // ─── ADMIN: all wallets ───────────────────────────────────────────────────

    @Override
    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }

    // ─── ADMIN: wallet by id ──────────────────────────────────────────────────

    @Override
    public Wallet getWalletById(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + id));
    }

    // ─── ADMIN: update points manually ───────────────────────────────────────

    @Override
    public Wallet updatePoints(Long id, int points) {
        Wallet wallet = getWalletById(id);
        wallet.setPoints(points);
        return walletRepository.save(wallet);
    }

    // ─── ADMIN: delete wallet ─────────────────────────────────────────────────

    @Override
    public void deleteWallet(Long id) {
        if (!walletRepository.existsById(id))
            throw new RuntimeException("Wallet not found with id: " + id);
        walletRepository.deleteById(id);
    }
}