package ra.edu.rikkeibank.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ra.edu.rikkeibank.account.entity.AccountStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String accountNumber;
    private String customerCode;
    private String accountTypeCode;
    private BigDecimal balance;
    private String currency;
    private AccountStatus status;
    private String branchCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
