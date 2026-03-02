package tn.esprit.pi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {

    private Long id;
    private double amount;
    private int earnedPoints;
    private LocalDateTime date;
    private String transactionType; // "DEPOSIT", "WITHDRAWAL", "TRANSFER"

    // User info
    private Long userId;
    private String username;
    private String email;

    // Wallet solde actuel
    private int currentPoints;
}