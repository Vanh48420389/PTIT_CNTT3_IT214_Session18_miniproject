package ra.edu.rikkeibank.transaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ra.edu.rikkeibank.transaction.dto.AccountResponseDto;
import ra.edu.rikkeibank.transaction.dto.ApiResponse;
import ra.edu.rikkeibank.transaction.dto.BalanceOperationRequestDto;
import ra.edu.rikkeibank.transaction.dto.BalanceOperationResponseDto;

@FeignClient(name = "account-service")
public interface AccountClient {

    @GetMapping("/api/accounts/{accountNumber}")
    ApiResponse<AccountResponseDto> getAccountByNumber(@PathVariable("accountNumber") String accountNumber);

    @PostMapping("/api/accounts/{accountNumber}/debit")
    ApiResponse<BalanceOperationResponseDto> debit(
            @PathVariable("accountNumber") String accountNumber,
            @RequestBody BalanceOperationRequestDto request);

    @PostMapping("/api/accounts/{accountNumber}/credit")
    ApiResponse<BalanceOperationResponseDto> credit(
            @PathVariable("accountNumber") String accountNumber,
            @RequestBody BalanceOperationRequestDto request);

    @PostMapping("/api/accounts/{accountNumber}/compensate-credit")
    ApiResponse<BalanceOperationResponseDto> compensateCredit(
            @PathVariable("accountNumber") String accountNumber,
            @RequestBody BalanceOperationRequestDto request);
}
