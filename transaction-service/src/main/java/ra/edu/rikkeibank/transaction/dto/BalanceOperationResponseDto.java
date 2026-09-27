package ra.edu.rikkeibank.transaction.dto;

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
public class BalanceOperationResponseDto {
    private String accountNumber;
    private BigDecimal previousBalance;
    private BigDecimal amount;
    private BigDecimal currentBalance;
    private String operationType;
    private String transactionRef;
    private LocalDateTime timestamp;
}
