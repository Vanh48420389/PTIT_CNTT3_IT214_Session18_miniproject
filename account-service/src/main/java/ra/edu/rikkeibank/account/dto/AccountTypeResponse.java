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
public class AccountTypeResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String typeCode;
    private String typeName;
    private String description;
    private Double interestRate;
    private BigDecimal minBalance;
    private BigDecimal dailyTransferLimit;
    private String currency;
    private AccountStatus status;
    private LocalDateTime createdAt;
}
