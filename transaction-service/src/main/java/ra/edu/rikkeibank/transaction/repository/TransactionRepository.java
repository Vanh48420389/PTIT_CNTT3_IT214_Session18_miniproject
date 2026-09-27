package ra.edu.rikkeibank.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ra.edu.rikkeibank.transaction.entity.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionRef(String transactionRef);

    List<Transaction> findBySourceAccountNumberOrDestinationAccountNumberOrderByCreatedAtDesc(
            String sourceAccount, String destinationAccount);

    List<Transaction> findByBranchCodeAndCreatedAtBetweenOrderByCreatedAtDesc(
            String branchCode, LocalDateTime start, LocalDateTime end);

    List<Transaction> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime start, LocalDateTime end);

    List<Transaction> findByInitiatedByOrderByCreatedAtDesc(String initiatedBy);
}
