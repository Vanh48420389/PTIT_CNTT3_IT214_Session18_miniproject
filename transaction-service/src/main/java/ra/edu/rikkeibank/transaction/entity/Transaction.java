package ra.edu.rikkeibank.transaction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 64)
    private String transactionRef; // UUID or TXN-xxxx

    @Column(nullable = false, length = 20)
    private String sourceAccountNumber;

    @Column(nullable = false, length = 20)
    private String destinationAccountNumber;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal fee = BigDecimal.ZERO;

    @Column(length = 255)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TransactionType type = TransactionType.TRANSFER;

    private String initiatedBy; // Username of customer or teller

    private String branchCode; // Assigned branch if teller initiated

    @Column(length = 500)
    private String failureReason; // Error details if FAILED or reason for COMPENSATED

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime completedAt;
}
