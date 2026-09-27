package ra.edu.rikkeibank.account.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ra.edu.rikkeibank.account.dto.AccountRequest;
import ra.edu.rikkeibank.account.dto.AccountResponse;
import ra.edu.rikkeibank.account.dto.ApiResponse;
import ra.edu.rikkeibank.account.dto.BalanceOperationRequest;
import ra.edu.rikkeibank.account.dto.BalanceOperationResponse;
import ra.edu.rikkeibank.account.entity.AccountStatus;
import ra.edu.rikkeibank.account.exception.ApiException;
import ra.edu.rikkeibank.account.service.AccountService;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts(
            @RequestParam(required = false) String branchCode,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (userRole != null && userRole.contains("CUSTOMER")) {
            throw ApiException.forbidden("Customers cannot list all bank accounts", "FORBIDDEN_ACCESS");
        }

        List<AccountResponse> list = (branchCode != null && !branchCode.isBlank())
                ? accountService.getAccountsByBranch(branchCode)
                : accountService.getAllAccounts();

        return ResponseEntity.ok(ApiResponse.success(list, "Retrieved accounts list"));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByNumber(@PathVariable String accountNumber) {
        AccountResponse response = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(ApiResponse.success(response, "Retrieved account information"));
    }

    @GetMapping("/customer/{customerCode}")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsByCustomer(@PathVariable String customerCode) {
        List<AccountResponse> list = accountService.getAccountsByCustomerCode(customerCode);
        return ResponseEntity.ok(ApiResponse.success(list, "Retrieved customer accounts"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody AccountRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (userRole != null && !userRole.contains("ADMIN") && !userRole.contains("TELLER")) {
            throw ApiException.forbidden("Only ADMIN or TELLER can open new bank accounts", "FORBIDDEN_ACCESS");
        }
        AccountResponse created = accountService.createAccount(request);
        return new ResponseEntity<>(ApiResponse.success(created, "Account created successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/{accountNumber}/debit")
    public ResponseEntity<ApiResponse<BalanceOperationResponse>> debit(
            @PathVariable String accountNumber,
            @Valid @RequestBody BalanceOperationRequest request) {
        BalanceOperationResponse response = accountService.debit(accountNumber, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Account debited successfully"));
    }

    @PostMapping("/{accountNumber}/credit")
    public ResponseEntity<ApiResponse<BalanceOperationResponse>> credit(
            @PathVariable String accountNumber,
            @Valid @RequestBody BalanceOperationRequest request) {
        BalanceOperationResponse response = accountService.credit(accountNumber, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Account credited successfully"));
    }

    @PostMapping("/{accountNumber}/compensate-credit")
    public ResponseEntity<ApiResponse<BalanceOperationResponse>> compensateCredit(
            @PathVariable String accountNumber,
            @Valid @RequestBody BalanceOperationRequest request) {
        BalanceOperationResponse response = accountService.compensateCredit(accountNumber, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Saga Compensation executed: Amount refunded to source account"));
    }

    @PutMapping("/{accountNumber}/status")
    public ResponseEntity<ApiResponse<AccountResponse>> updateStatus(
            @PathVariable String accountNumber,
            @RequestParam AccountStatus status,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (userRole != null && !userRole.contains("ADMIN")) {
            throw ApiException.forbidden("Only ADMIN can freeze or change account status", "FORBIDDEN_ADMIN_ONLY");
        }
        AccountResponse response = accountService.updateStatus(accountNumber, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Account status updated to " + status));
    }
}
