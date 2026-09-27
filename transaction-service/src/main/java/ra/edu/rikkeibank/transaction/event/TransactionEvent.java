package ra.edu.rikkeibank.transaction.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventType; // TRANSACTION_COMPLETED, TRANSACTION_FAILED, TRANSACTION_COMPENSATED
    private String transactionRef;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private String status;
    private String message;
    private String branchCode;
    private String failureReason;
    @Builder.Default
    private String timestamp = LocalDateTime.now().toString();
}
