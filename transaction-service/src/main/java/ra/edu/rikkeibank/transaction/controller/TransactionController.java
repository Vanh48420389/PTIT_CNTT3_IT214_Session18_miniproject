package ra.edu.rikkeibank.transaction.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ra.edu.rikkeibank.transaction.dto.ApiResponse;
import ra.edu.rikkeibank.transaction.dto.TransactionResponse;
import ra.edu.rikkeibank.transaction.dto.TransferRequest;
import ra.edu.rikkeibank.transaction.entity.TransactionAudit;
import ra.edu.rikkeibank.transaction.exception.ApiException;
import ra.edu.rikkeibank.transaction.service.TransactionService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transferMoney(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader(value = "X-User-Name", required = false) String username,
            @RequestHeader(value = "X-User-Branch", required = false) String userBranch) {
        TransactionResponse response = transactionService.transfer(request, username, userBranch);
        String message = "COMPENSATED".equalsIgnoreCase(response.getStatus().name())
                ? "Saga Rollback executed: Destination step failed, funds safely refunded to source account"
                : "Transfer completed successfully";
        return new ResponseEntity<>(ApiResponse.success(response, message), HttpStatus.OK);
    }

    @GetMapping("/{transactionRef}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionByRef(
            @PathVariable String transactionRef) {
        TransactionResponse response = transactionService.getTransactionByRef(transactionRef);
        return ResponseEntity.ok(ApiResponse.success(response, "Retrieved transaction detail"));
    }

    @GetMapping("/{transactionRef}/audits")
    public ResponseEntity<ApiResponse<List<TransactionAudit>>> getTransactionAudits(
            @PathVariable String transactionRef) {
        List<TransactionAudit> audits = transactionService.getTransactionAudits(transactionRef);
        return ResponseEntity.ok(ApiResponse.success(audits, "Retrieved Saga transaction audit trail"));
    }

    @GetMapping("/my-transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getMyTransactions(
            @RequestParam String accountNumber) {
        List<TransactionResponse> list = transactionService.getMyTransactions(accountNumber);
        return ResponseEntity.ok(ApiResponse.success(list, "Retrieved transaction history for account " + accountNumber));
    }

    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getDailyTransactions(
            @RequestParam(required = false) String branchCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-User-Branch", required = false) String tellerBranch) {

        // Strict role validation:
        // TELLER can only view transactions within their assigned branch
        String effectiveBranch = branchCode;
        if (userRole != null && userRole.contains("TELLER")) {
            if (tellerBranch != null && !tellerBranch.isBlank()) {
                effectiveBranch = tellerBranch; // Force teller branch scope
            }
        } else if (userRole != null && userRole.contains("CUSTOMER")) {
            throw ApiException.forbidden("Customers cannot access teller daily transaction reports", "FORBIDDEN_ACCESS");
        }

        List<TransactionResponse> list = transactionService.getDailyTransactions(effectiveBranch, date);
        return ResponseEntity.ok(ApiResponse.success(list, "Retrieved daily transactions report for branch: " + (effectiveBranch != null ? effectiveBranch : "ALL")));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactions(
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (userRole != null && !userRole.contains("ADMIN")) {
            throw ApiException.forbidden("Only ADMIN can view entire transaction ledger", "FORBIDDEN_ADMIN_ONLY");
        }
        List<TransactionResponse> list = transactionService.getAllTransactions();
        return ResponseEntity.ok(ApiResponse.success(list, "Retrieved all system transactions"));
    }
}
