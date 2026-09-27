package ra.edu.rikkeibank.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ra.edu.rikkeibank.transaction.entity.TransactionAudit;

import java.util.List;

@Repository
public interface TransactionAuditRepository extends JpaRepository<TransactionAudit, Long> {
    List<TransactionAudit> findByTransactionRefOrderByTimestampAsc(String transactionRef);
}
