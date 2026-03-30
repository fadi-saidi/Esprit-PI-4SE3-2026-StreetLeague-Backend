package tn.esprit.pi.service.wallet;

import tn.esprit.pi.domain.Transaction;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.domain.Wallet;
import tn.esprit.pi.dto.TransactionResponse;

import java.util.List;

public interface IWalletService {

    // ─── User operations ──────────────────────────────────────────────────────
    Wallet getOrCreateWallet(User user);
    Wallet getWallet(User user);
    Wallet deposit(User user, Double amount);
    Wallet withdraw(User user, Double amount);
    void transfer(User from, Long toUserId, Double amount);
    List<TransactionResponse> getHistory(User user);

    // ─── Admin operations ──────────────────────────────────────────────────────
    List<Wallet> getAllWallets();
    Wallet getWalletById(Long id);
    Wallet updatePoints(Long id, int points);
    void deleteWallet(Long id);
}