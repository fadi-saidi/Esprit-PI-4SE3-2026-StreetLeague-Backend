package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.dto.TransactionResponse;
import tn.esprit.pi.domain.User;
import tn.esprit.pi.domain.Wallet;
import tn.esprit.pi.dto.Dtos;
import tn.esprit.pi.dto.TransactionResponse;
import tn.esprit.pi.repository.UserRepository;
import tn.esprit.pi.service.wallet.IWalletService;

import java.util.List;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final IWalletService walletService;
    private final UserRepository userRepository;

    // ─── Helper: resolve User from JWT ───────────────────────────────────────

    private User resolveUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // =========================================================
    //  USER endpoints  (authenticated)
    // =========================================================

    /** GET /wallet/me */
    @GetMapping("/me")
    public ResponseEntity<Wallet> getMyWallet(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(walletService.getOrCreateWallet(resolveUser(ud)));
    }

    /** POST /wallet/deposit — body: { "amount": 100.0 } */
    @PostMapping("/deposit")
    public ResponseEntity<Wallet> deposit(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody Dtos.DepositRequest req) {
        return ResponseEntity.ok(walletService.deposit(resolveUser(ud), req.amount()));
    }

    /** POST /wallet/withdraw — body: { "amount": 50.0 } */
    @PostMapping("/withdraw")
    public ResponseEntity<Wallet> withdraw(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody Dtos.DepositRequest req) {
        return ResponseEntity.ok(walletService.withdraw(resolveUser(ud), req.amount()));
    }

    /** POST /wallet/transfer — body: { "toUserId": 5, "amount": 20.0 } */
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody Dtos.TransferRequest req) {
        walletService.transfer(resolveUser(ud), req.toUserId(), req.amount());
        return ResponseEntity.ok("Transfer successful");
    }

    /** GET /wallet/history */
    @GetMapping("/history")
    public ResponseEntity<List<TransactionResponse>> history(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(walletService.getHistory(resolveUser(ud)));
    }

    // =========================================================
    //  ADMIN endpoints  →  /wallet/admin/**
    // =========================================================

    @GetMapping("/admin/all")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<Dtos.WalletAdminDTO>> getAllWallets() {
        return ResponseEntity.ok(walletService.getAllWalletsAdmin());
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<Wallet> getById(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.getWalletById(id));
    }

    @PutMapping("/admin/{id}/points")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<Wallet> updatePoints(
            @PathVariable Long id,
            @RequestParam int points) {
        return ResponseEntity.ok(walletService.updatePoints(id, points));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> deleteWallet(@PathVariable Long id) {
        walletService.deleteWallet(id);
        return ResponseEntity.ok("Wallet deleted successfully");
    }
}
