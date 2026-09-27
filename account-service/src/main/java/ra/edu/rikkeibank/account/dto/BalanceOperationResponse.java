package ra.edu.rikkeibank.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceOperationResponse {
    private String accountNumber;
    private BigDecimal previousBalance;
    private BigDecimal amount;
    private BigDecimal currentBalance;
    private String operationType; // DEBIT, CREDIT, COMPENSATE
    private String transactionRef;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
