package ra.edu.rikkeibank.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ra.edu.rikkeibank.account.entity.Account;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByCustomerCode(String customerCode);
    List<Account> findByBranchCode(String branchCode);
    boolean existsByAccountNumber(String accountNumber);
}
