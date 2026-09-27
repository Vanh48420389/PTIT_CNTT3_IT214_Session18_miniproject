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
public class AccountResponseDto {
    private Long id;
    private String accountNumber;
    private String customerCode;
    private String accountTypeCode;
    private BigDecimal balance;
    private String currency;
    private String status; // ACTIVE, FROZEN, CLOSED
    private String branchCode;
}
