package tn.esprit.pi.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double amount;

    private int earnedPoints;

    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    private String description;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}