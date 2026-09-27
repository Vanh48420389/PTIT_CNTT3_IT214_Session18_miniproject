package ra.edu.rikkeibank.transaction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_audits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String transactionRef;

    @Column(nullable = false, length = 50)
    private String stepName; // VALIDATION, DEBIT_SOURCE, CREDIT_DEST, COMPENSATE_REFUND, KAFKA_PUBLISH

    @Column(nullable = false, length = 30)
    private String status; // STARTED, COMPLETED, FAILED

    @Column(length = 1000)
    private String details;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
