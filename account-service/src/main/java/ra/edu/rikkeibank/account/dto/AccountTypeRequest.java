package ra.edu.rikkeibank.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ra.edu.rikkeibank.account.entity.AccountStatus;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountTypeRequest {
    @NotBlank(message = "Type code cannot be blank")
    private String typeCode;

    @NotBlank(message = "Type name cannot be blank")
    private String typeName;

    private String description;

    @NotNull(message = "Interest rate cannot be null")
    private Double interestRate;

    @NotNull(message = "Minimum balance cannot be null")
    @DecimalMin(value = "0.0", message = "Minimum balance must be >= 0")
    private BigDecimal minBalance;

    @NotNull(message = "Daily transfer limit cannot be null")
    @DecimalMin(value = "1.0", message = "Daily transfer limit must be > 0")
    private BigDecimal dailyTransferLimit;

    private String currency;
    private AccountStatus status;
}
