package ra.edu.rikkeibank.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceOperationRequestDto {
    private BigDecimal amount;
    private String transactionRef;
    private String description;
}
