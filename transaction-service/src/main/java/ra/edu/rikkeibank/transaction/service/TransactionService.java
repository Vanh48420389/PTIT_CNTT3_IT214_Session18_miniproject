package ra.edu.rikkeibank.transaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ra.edu.rikkeibank.transaction.dto.TransactionResponse;
import ra.edu.rikkeibank.transaction.dto.TransferRequest;
import ra.edu.rikkeibank.transaction.entity.Transaction;
import ra.edu.rikkeibank.transaction.entity.TransactionAudit;
import ra.edu.rikkeibank.transaction.exception.ApiException;
import ra.edu.rikkeibank.transaction.repository.TransactionAuditRepository;
import ra.edu.rikkeibank.transaction.repository.TransactionRepository;
import ra.edu.rikkeibank.transaction.saga.TransferSagaOrchestrator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionAuditRepository auditRepository;
    private final TransferSagaOrchestrator sagaOrchestrator;

    public TransactionResponse transfer(TransferRequest request, String initiatedBy, String branchCode) {
        Transaction result = sagaOrchestrator.executeTransferSaga(request, initiatedBy, branchCode);
        return mapToResponse(result);
    }

    public TransactionResponse getTransactionByRef(String transactionRef) {
        Transaction transaction = transactionRepository.findByTransactionRef(transactionRef)
                .orElseThrow(() -> ApiException.notFound("Transaction not found: " + transactionRef, "TRANSACTION_NOT_FOUND"));
        return mapToResponse(transaction);
    }

    public List<TransactionResponse> getMyTransactions(String accountNumber) {
        return transactionRepository.findBySourceAccountNumberOrDestinationAccountNumberOrderByCreatedAtDesc(accountNumber, accountNumber)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> getDailyTransactions(String branchCode, LocalDate date) {
        LocalDate queryDate = date != null ? date : LocalDate.now();
        LocalDateTime startOfDay = queryDate.atStartOfDay();
        LocalDateTime endOfDay = queryDate.atTime(LocalTime.MAX);

        List<Transaction> transactions;
        if (branchCode != null && !branchCode.isBlank()) {
            transactions = transactionRepository.findByBranchCodeAndCreatedAtBetweenOrderByCreatedAtDesc(branchCode, startOfDay, endOfDay);
        } else {
            transactions = transactionRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startOfDay, endOfDay);
        }

        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TransactionAudit> getTransactionAudits(String transactionRef) {
        return auditRepository.findByTransactionRefOrderByTimestampAsc(transactionRef);
    }

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse mapToResponse(Transaction txn) {
        return TransactionResponse.builder()
                .id(txn.getId())
                .transactionRef(txn.getTransactionRef())
                .sourceAccountNumber(txn.getSourceAccountNumber())
                .destinationAccountNumber(txn.getDestinationAccountNumber())
                .amount(txn.getAmount())
                .fee(txn.getFee())
                .message(txn.getMessage())
                .status(txn.getStatus())
                .type(txn.getType())
                .initiatedBy(txn.getInitiatedBy())
                .branchCode(txn.getBranchCode())
                .failureReason(txn.getFailureReason())
                .createdAt(txn.getCreatedAt())
                .completedAt(txn.getCompletedAt())
                .build();
    }
}
