package ra.edu.rikkeibank.customer.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ra.edu.rikkeibank.customer.entity.Customer;
import ra.edu.rikkeibank.customer.entity.CustomerStatus;
import ra.edu.rikkeibank.customer.entity.Staff;
import ra.edu.rikkeibank.customer.repository.CustomerRepository;
import ra.edu.rikkeibank.customer.repository.StaffRepository;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerDataInitializer implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;

    @Override
    public void run(String... args) {
        if (customerRepository.count() == 0) {
            log.info("Seeding initial customers...");

            customerRepository.save(Customer.builder()
                    .customerCode("CUST001")
                    .fullName("Le Van Khach Hang 1")
                    .email("customer1@gmail.com")
                    .phoneNumber("0934567890")
                    .identityCard("001200001111")
                    .address("123 Hoang Hoa Tham, Ba Dinh, Ha Noi")
                    .dateOfBirth(LocalDate.of(1995, 5, 20))
                    .branchCode("BRANCH_HN")
                    .status(CustomerStatus.ACTIVE)
                    .build());

            customerRepository.save(Customer.builder()
                    .customerCode("CUST002")
                    .fullName("Pham Thi Khach Hang 2")
                    .email("customer2@gmail.com")
                    .phoneNumber("0945678901")
                    .identityCard("079200002222")
                    .address("456 Nguyen Hue, Quan 1, TP HCM")
                    .dateOfBirth(LocalDate.of(1998, 8, 15))
                    .branchCode("BRANCH_HCM")
                    .status(CustomerStatus.ACTIVE)
                    .build());
        }

        if (staffRepository.count() == 0) {
            log.info("Seeding initial staff...");

            staffRepository.save(Staff.builder()
                    .staffCode("TELLER001")
                    .fullName("Nguyen Van Teller - Ha Noi")
                    .email("teller.hn@rikkeibank.com")
                    .phoneNumber("0912345678")
                    .role("TELLER")
                    .branchCode("BRANCH_HN")
                    .status(CustomerStatus.ACTIVE)
                    .build());

            staffRepository.save(Staff.builder()
                    .staffCode("TELLER002")
                    .fullName("Tran Thi Teller - HCM")
                    .email("teller.hcm@rikkeibank.com")
                    .phoneNumber("0923456789")
                    .role("TELLER")
                    .branchCode("BRANCH_HCM")
                    .status(CustomerStatus.ACTIVE)
                    .build());
        }
    }
}
