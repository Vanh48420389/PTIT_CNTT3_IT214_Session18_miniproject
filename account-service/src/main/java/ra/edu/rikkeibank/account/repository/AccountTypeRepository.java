package ra.edu.rikkeibank.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ra.edu.rikkeibank.account.entity.AccountType;

import java.util.Optional;

@Repository
public interface AccountTypeRepository extends JpaRepository<AccountType, Long> {
    Optional<AccountType> findByTypeCode(String typeCode);
    boolean existsByTypeCode(String typeCode);
}
