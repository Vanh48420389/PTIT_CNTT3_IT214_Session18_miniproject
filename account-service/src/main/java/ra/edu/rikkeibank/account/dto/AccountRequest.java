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
public class AccountRequest {
    @NotBlank(message = "Account number cannot be blank")
    private String accountNumber;

    @NotBlank(message = "Customer code cannot be blank")
    private String customerCode;

    @NotBlank(message = "Account type code cannot be blank (e.g. CHECKING, SAVINGS)")
    private String accountTypeCode;

    @NotNull(message = "Initial balance cannot be null")
    @DecimalMin(value = "0.0", message = "Initial balance must be >= 0")
    private BigDecimal initialBalance;

    private String currency;
    private String branchCode;
    private AccountStatus status;
}
