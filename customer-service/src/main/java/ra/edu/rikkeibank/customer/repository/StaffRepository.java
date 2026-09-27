package ra.edu.rikkeibank.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ra.edu.rikkeibank.customer.entity.Staff;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByStaffCode(String staffCode);
    List<Staff> findByBranchCode(String branchCode);
    boolean existsByStaffCode(String staffCode);
}
