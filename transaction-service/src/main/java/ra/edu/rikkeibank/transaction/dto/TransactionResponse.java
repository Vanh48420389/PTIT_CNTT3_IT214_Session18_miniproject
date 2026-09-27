package ra.edu.rikkeibank.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ra.edu.rikkeibank.transaction.entity.TransactionStatus;
import ra.edu.rikkeibank.transaction.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private String transactionRef;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private BigDecimal fee;
    private String message;
    private TransactionStatus status;
    private TransactionType type;
    private String initiatedBy;
    private String branchCode;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
