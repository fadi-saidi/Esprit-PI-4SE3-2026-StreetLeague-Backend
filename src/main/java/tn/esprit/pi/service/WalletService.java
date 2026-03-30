package tn.esprit.pi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi.domain.*;
import tn.esprit.pi.repository.TransactionRepository;
import tn.esprit.pi.repository.WalletRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    // Points calculation: 1% of purchase amount as points
    private static final double POINTS_RATE = 0.01;
    
    // Commission for player merchandise sales (10%)
    private static final double PLAYER_COMMISSION_RATE = 0.90;

    /**
     * Process payment for order - deducts amount and adds points
     */
    public void processOrderPayment(User buyer, double amount, Long orderId) {
        Wallet wallet = getOrCreateWallet(buyer);
        
        // Convert purchase amount to points for deduction (1:1 ratio)
        int pointsToDeduct = (int) Math.round(amount);
        
        // Check if wallet has sufficient balance
        if (wallet.getPoints() < pointsToDeduct) {
            throw new RuntimeException("Insufficient wallet balance. Required: " + pointsToDeduct + " points, Available: " + wallet.getPoints() + " points");
        }
        
        // Deduct payment amount from wallet
        wallet.setPoints(wallet.getPoints() - pointsToDeduct);
        
        // Add loyalty points (1% of purchase amount)
        int loyaltyPoints = (int) Math.round(amount * POINTS_RATE);
        wallet.setPoints(wallet.getPoints() + loyaltyPoints);
        
        walletRepository.save(wallet);
        
        // Create transaction record
        Transaction transaction = Transaction.builder()
                .user(buyer)
                .amount(-amount) // Negative for payment
                .earnedPoints(loyaltyPoints)
                .type(TransactionType.PAYMENT)
                .description("Order payment #" + orderId + " (Deducted: " + pointsToDeduct + " points, Earned: " + loyaltyPoints + " loyalty points)")
                .date(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);
    }

    /**
     * Process player merchandise sale - adds revenue to seller's wallet
     */
    public void processPlayerMerchSale(User seller, double saleAmount, String productName) {
        Wallet sellerWallet = getOrCreateWallet(seller);
        
        // Calculate seller revenue (90% of sale, 10% platform commission)
        double sellerRevenue = saleAmount * PLAYER_COMMISSION_RATE;
        int pointsEarned = (int) Math.round(sellerRevenue);
        
        // Add revenue as points (1:1 ratio)
        sellerWallet.setPoints(sellerWallet.getPoints() + pointsEarned);
        walletRepository.save(sellerWallet);
        
        // Create transaction record
        Transaction transaction = Transaction.builder()
                .user(seller)
                .amount(sellerRevenue)
                .earnedPoints(pointsEarned)
                .type(TransactionType.DEPOSIT)
                .description("Merchandise sale: " + productName)
                .date(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);
    }

    /**
     * Process refund for cancelled order
     */
    public void processOrderRefund(User buyer, double amount, Long orderId) {
        Wallet wallet = getOrCreateWallet(buyer);
        
        // Add refund amount as points (1:1 ratio)
        int pointsRefunded = (int) Math.round(amount);
        wallet.setPoints(wallet.getPoints() + pointsRefunded);
        walletRepository.save(wallet);
        
        // Create transaction record
        Transaction transaction = Transaction.builder()
                .user(buyer)
                .amount(amount)
                .earnedPoints(pointsRefunded)
                .type(TransactionType.REFUND)
                .description("Order refund #" + orderId)
                .date(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);
    }

    /**
     * Get or create wallet for user
     */
    private Wallet getOrCreateWallet(User user) {
        return walletRepository.findByUser(user)
                .orElseGet(() -> {
                    Wallet newWallet = Wallet.builder()
                            .user(user)
                            .points(0)
                            .build();
                    return walletRepository.save(newWallet);
                });
    }

    /**
     * Get wallet balance for user
     */
    public int getWalletPoints(User user) {
        return walletRepository.findByUser(user)
                .map(Wallet::getPoints)
                .orElse(0);
    }
}