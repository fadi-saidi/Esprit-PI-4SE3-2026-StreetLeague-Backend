package tn.esprit.pi.service.wallet;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.domain.Transaction;
import tn.esprit.pi.domain.TransactionType;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.domain.Wallet;
import tn.esprit.pi.dto.Dtos;
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

    @Override
    public Wallet getWallet(User user) {
        return walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + user.getEmail()));
    }

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
                .transactionType(TransactionType.DEPOSIT)
                .user(user)
                .build());

        return wallet;
    }

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
                .transactionType(TransactionType.WITHDRAWAL)
                .user(user)
                .build());

        return wallet;
    }

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

        fromWallet.setPoints(fromWallet.getPoints() - points);
        walletRepository.save(fromWallet);

        Wallet toWallet = getOrCreateWallet(to);
        toWallet.setPoints(toWallet.getPoints() + points);
        walletRepository.save(toWallet);

        LocalDateTime now = LocalDateTime.now();

        transactionRepository.save(Transaction.builder()
                .amount(-amount)
                .earnedPoints(-points)
                .date(now)
                .transactionType(TransactionType.TRANSFER)
                .user(from)
                .build());

        transactionRepository.save(Transaction.builder()
                .amount(amount)
                .earnedPoints(points)
                .date(now)
                .transactionType(TransactionType.TRANSFER)
                .user(to)
                .build());
    }

    @Override
    public List<TransactionResponse> getHistory(User user) {
        return transactionRepository.findByUser(user).stream().map(t -> {
            String type;
            if (t.getTransactionType() != null) {
                type = t.getTransactionType().name();
            } else {
                // fallback pour les anciens enregistrements sans type
                if (t.getAmount() > 0) type = "DEPOSIT";
                else type = "WITHDRAWAL";
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

    @Override
    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }

    @Override
    public Wallet getWalletById(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + id));
    }

    @Override
    public Wallet updatePoints(Long id, int points) {
        Wallet wallet = getWalletById(id);
        wallet.setPoints(points);
        return walletRepository.save(wallet);
    }

    @Override
    public void deleteWallet(Long id) {
        if (!walletRepository.existsById(id))
            throw new RuntimeException("Wallet not found with id: " + id);
        walletRepository.deleteById(id);
    }

    @Override
    public List<Dtos.WalletAdminDTO> getAllWalletsAdmin() {
        return walletRepository.findAllWalletsRaw()
                .stream()
                .map(row -> new Dtos.WalletAdminDTO(
                        row[0] != null ? ((Number) row[0]).longValue() : null,
                        row[1] != null ? ((Number) row[1]).intValue() : 0,
                        row[2] != null ? ((Number) row[2]).longValue() : null,
                        row[3] != null ? (String) row[3] : "Unknown",
                        row[4] != null ? (String) row[4] : "",
                        row[5] != null ? (String) row[5] : "",
                        row[6] != null ? (String) row[6] : ""
                ))
                .collect(Collectors.toList());
    }
}