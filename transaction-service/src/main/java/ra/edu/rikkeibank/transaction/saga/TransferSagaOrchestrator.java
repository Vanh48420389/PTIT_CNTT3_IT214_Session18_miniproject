package ra.edu.rikkeibank.transaction.saga;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ra.edu.rikkeibank.transaction.client.AccountClient;
import ra.edu.rikkeibank.transaction.dto.*;
import ra.edu.rikkeibank.transaction.entity.Transaction;
import ra.edu.rikkeibank.transaction.entity.TransactionAudit;
import ra.edu.rikkeibank.transaction.entity.TransactionStatus;
import ra.edu.rikkeibank.transaction.entity.TransactionType;
import ra.edu.rikkeibank.transaction.event.TransactionEvent;
import ra.edu.rikkeibank.transaction.exception.ApiException;
import ra.edu.rikkeibank.transaction.repository.TransactionAuditRepository;
import ra.edu.rikkeibank.transaction.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferSagaOrchestrator {

    private final TransactionRepository transactionRepository;
    private final TransactionAuditRepository auditRepository;
    private final AccountClient accountClient;

    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_TRANSACTIONS = "transaction-events";

    @CircuitBreaker(name = "accountService", fallbackMethod = "transferCircuitBreakerFallback")
    @Transactional
    public Transaction executeTransferSaga(TransferRequest request, String initiatedBy, String branchCode) {
        String txnRef = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Starting Saga Transfer Orchestration [{}]: from {} to {} amount: {}",
                txnRef, request.getSourceAccountNumber(), request.getDestinationAccountNumber(), request.getAmount());

        // 1. Validation
        if (request.getSourceAccountNumber().equals(request.getDestinationAccountNumber())) {
            throw ApiException.badRequest("Source and destination accounts cannot be identical", "IDENTICAL_ACCOUNTS");
        }

        Transaction transaction = Transaction.builder()
                .transactionRef(txnRef)
                .sourceAccountNumber(request.getSourceAccountNumber())
                .destinationAccountNumber(request.getDestinationAccountNumber())
                .amount(request.getAmount())
                .message(request.getMessage() != null ? request.getMessage() : "Chuyen tien qua RikkeiBank")
                .status(TransactionStatus.PENDING)
                .type(TransactionType.TRANSFER)
                .initiatedBy(initiatedBy != null ? initiatedBy : "SYSTEM")
                .branchCode(branchCode != null ? branchCode : "BRANCH_HN")
                .build();

        transaction = transactionRepository.save(transaction);
        logAudit(txnRef, "VALIDATION", "STARTED", "Initiating pre-transfer validation");

        // 2. Fetch and Validate Source Account
        ApiResponse<AccountResponseDto> srcResponse = accountClient.getAccountByNumber(request.getSourceAccountNumber());
        if (srcResponse == null || srcResponse.getData() == null) {
            failTransaction(transaction, "Source account not found: " + request.getSourceAccountNumber());
            throw ApiException.notFound("Source account not found", "ACCOUNT_NOT_FOUND");
        }
        AccountResponseDto srcAccount = srcResponse.getData();
        if (!"ACTIVE".equalsIgnoreCase(srcAccount.getStatus())) {
            failTransaction(transaction, "Source account is not active: " + srcAccount.getStatus());
            throw ApiException.badRequest("Source account is " + srcAccount.getStatus(), "ACCOUNT_NOT_ACTIVE");
        }
        if (srcAccount.getBalance().compareTo(request.getAmount()) < 0) {
            failTransaction(transaction, "Insufficient balance: Available " + srcAccount.getBalance() + " < Transfer " + request.getAmount());
            throw ApiException.badRequest("Insufficient balance for transfer", "INSUFFICIENT_FUNDS");
        }

        // 3. Fetch and Validate Destination Account
        ApiResponse<AccountResponseDto> destResponse = accountClient.getAccountByNumber(request.getDestinationAccountNumber());
        if (destResponse == null || destResponse.getData() == null) {
            failTransaction(transaction, "Destination account not found: " + request.getDestinationAccountNumber());
            throw ApiException.notFound("Destination account not found", "ACCOUNT_NOT_FOUND");
        }
        AccountResponseDto destAccount = destResponse.getData();
        if (!"ACTIVE".equalsIgnoreCase(destAccount.getStatus())) {
            failTransaction(transaction, "Destination account is not active: " + destAccount.getStatus());
            throw ApiException.badRequest("Destination account is " + destAccount.getStatus(), "ACCOUNT_NOT_ACTIVE");
        }

        logAudit(txnRef, "VALIDATION", "COMPLETED", "Both accounts verified and active");

        // 4. Saga Step 1: Debit Source Account
        logAudit(txnRef, "DEBIT_SOURCE", "STARTED", "Deducting " + request.getAmount() + " from " + request.getSourceAccountNumber());
        try {
            BalanceOperationRequestDto debitReq = BalanceOperationRequestDto.builder()
                    .amount(request.getAmount())
                    .transactionRef(txnRef)
                    .description("Chuyen tien den " + request.getDestinationAccountNumber())
                    .build();
            ApiResponse<BalanceOperationResponseDto> debitRes = accountClient.debit(request.getSourceAccountNumber(), debitReq);
            if (debitRes == null || !debitRes.isSuccess()) {
                failTransaction(transaction, "Debit step failed on source account");
                throw ApiException.badRequest("Debit operation failed", "DEBIT_FAILED");
            }
            logAudit(txnRef, "DEBIT_SOURCE", "COMPLETED", "Debited successfully. Balance deducted.");
        } catch (Exception e) {
            failTransaction(transaction, "Failed to debit source account: " + e.getMessage());
            logAudit(txnRef, "DEBIT_SOURCE", "FAILED", e.getMessage());
            throw ApiException.badRequest("Debit failed: " + e.getMessage(), "DEBIT_ERROR");
        }

        // 5. Saga Step 2: Credit Destination Account (or trigger simulated failure)
        boolean simulateFail = "DEST_FAIL".equalsIgnoreCase(request.getSimulateFailure());
        logAudit(txnRef, "CREDIT_DEST", "STARTED", "Crediting " + request.getAmount() + " to " + request.getDestinationAccountNumber());

        if (simulateFail) {
            log.warn("Saga simulation flag [DEST_FAIL] detected! Triggering compensating rollback...");
            executeCompensation(transaction, request, txnRef, "SIMULATED_DESTINATION_FAILURE: Destination banking node rejected credit");
            return transaction;
        }

        try {
            BalanceOperationRequestDto creditReq = BalanceOperationRequestDto.builder()
                    .amount(request.getAmount())
                    .transactionRef(txnRef)
                    .description("Nhan tien tu " + request.getSourceAccountNumber())
                    .build();
            ApiResponse<BalanceOperationResponseDto> creditRes = accountClient.credit(request.getDestinationAccountNumber(), creditReq);
            if (creditRes == null || !creditRes.isSuccess()) {
                executeCompensation(transaction, request, txnRef, "Credit response indicated failure from account-service");
                return transaction;
            }
            logAudit(txnRef, "CREDIT_DEST", "COMPLETED", "Credited successfully. Destination balance increased.");
        } catch (Exception e) {
            log.error("Exception occurred during destination credit: {}. Initiating Saga Compensating Rollback!", e.getMessage());
            executeCompensation(transaction, request, txnRef, "Destination credit threw exception: " + e.getMessage());
            return transaction;
        }

        // 6. Saga Completed Successfully!
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCompletedAt(LocalDateTime.now());
        Transaction saved = transactionRepository.save(transaction);
        logAudit(txnRef, "SAGA_COMPLETE", "COMPLETED", "Transfer Saga completed successfully");

        // 7. Publish Event to Kafka
        publishEvent("TRANSACTION_COMPLETED", saved, null);

        return saved;
    }

    /**
     * Saga Compensating Transaction:
     * When Destination Credit fails, this step executes to refund the debited amount back to the source account.
     */
    private void executeCompensation(Transaction transaction, TransferRequest request, String txnRef, String reason) {
        log.warn("Executing Saga Compensation for transaction [{}]: refunding {} to {}",
                txnRef, request.getAmount(), request.getSourceAccountNumber());
        logAudit(txnRef, "COMPENSATE_REFUND", "STARTED", "Refunding debited amount due to: " + reason);

        try {
            BalanceOperationRequestDto compensateReq = BalanceOperationRequestDto.builder()
                    .amount(request.getAmount())
                    .transactionRef(txnRef)
                    .description("Hoan tien giao dich chuyen khoan loi [" + txnRef + "]")
                    .build();
            accountClient.compensateCredit(request.getSourceAccountNumber(), compensateReq);
            logAudit(txnRef, "COMPENSATE_REFUND", "COMPLETED", "Refund completed. Source balance restored perfectly.");
        } catch (Exception e) {
            log.error("CRITICAL: Saga compensation refund failed for {}: {}", txnRef, e.getMessage());
            logAudit(txnRef, "COMPENSATE_REFUND", "FAILED", "Critical: Compensation refund failed: " + e.getMessage());
        }

        transaction.setStatus(TransactionStatus.COMPENSATED);
        transaction.setFailureReason("SAGA ROLLBACK: " + reason);
        transaction.setCompletedAt(LocalDateTime.now());
        Transaction saved = transactionRepository.save(transaction);

        publishEvent("TRANSACTION_COMPENSATED", saved, reason);
    }

    private void failTransaction(Transaction transaction, String reason) {
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setFailureReason(reason);
        transaction.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
        logAudit(transaction.getTransactionRef(), "TRANSACTION_FAILED", "COMPLETED", reason);
        publishEvent("TRANSACTION_FAILED", transaction, reason);
    }

    private void publishEvent(String eventType, Transaction transaction, String reason) {
        TransactionEvent event = TransactionEvent.builder()
                .eventType(eventType)
                .transactionRef(transaction.getTransactionRef())
                .sourceAccountNumber(transaction.getSourceAccountNumber())
                .destinationAccountNumber(transaction.getDestinationAccountNumber())
                .amount(transaction.getAmount())
                .status(transaction.getStatus().name())
                .message(transaction.getMessage())
                .branchCode(transaction.getBranchCode())
                .failureReason(reason)
                .build();

        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send(TOPIC_TRANSACTIONS, transaction.getTransactionRef(), event);
                log.info("Published Kafka event [{}] for transaction ref: {}", eventType, transaction.getTransactionRef());
            } catch (Exception e) {
                log.warn("Kafka broker not reachable to send event: {}. Continuing without blocking transaction flow.", e.getMessage());
            }
        }
    }

    private void logAudit(String txnRef, String step, String status, String details) {
        TransactionAudit audit = TransactionAudit.builder()
                .transactionRef(txnRef)
                .stepName(step)
                .status(status)
                .details(details)
                .build();
        auditRepository.save(audit);
    }

    /**
     * Resilience4j Fallback Method when account-service is down / circuit is open
     */
    public Transaction transferCircuitBreakerFallback(TransferRequest request, String initiatedBy, String branchCode, Throwable ex) {
        log.error("Circuit Breaker [accountService] OPEN / Fallback triggered: {}", ex.getMessage());
        throw ApiException.serviceUnavailable(
                "Account Service is currently unavailable or circuit is OPEN. Request safely aborted to prevent cascading failure. Details: " + ex.getMessage(),
                "CIRCUIT_BREAKER_OPEN"
        );
    }
}
