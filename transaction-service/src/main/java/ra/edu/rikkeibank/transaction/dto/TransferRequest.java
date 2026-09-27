package ra.edu.rikkeibank.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    @NotBlank(message = "Source account number cannot be blank")
    private String sourceAccountNumber;

    @NotBlank(message = "Destination account number cannot be blank")
    private String destinationAccountNumber;

    @NotNull(message = "Transfer amount cannot be null")
    @DecimalMin(value = "1000.0", message = "Minimum transfer amount is 1,000 VND")
    private BigDecimal amount;

    private String message;

    // Optional simulation mode for testing Saga Rollback ("DEST_FAIL", "CIRCUIT_BREAKER_FAIL", or null)
    private String simulateFailure;
}
