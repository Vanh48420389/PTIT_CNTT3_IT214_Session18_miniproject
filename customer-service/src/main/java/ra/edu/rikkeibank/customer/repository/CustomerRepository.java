package ra.edu.rikkeibank.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ra.edu.rikkeibank.customer.entity.Customer;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCustomerCode(String customerCode);
    Optional<Customer> findByIdentityCard(String identityCard);
    Optional<Customer> findByEmail(String email);
    List<Customer> findByBranchCode(String branchCode);
    boolean existsByCustomerCode(String customerCode);
    boolean existsByIdentityCard(String identityCard);
    boolean existsByEmail(String email);
}
