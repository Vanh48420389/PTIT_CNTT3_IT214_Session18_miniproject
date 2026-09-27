package ra.edu.rikkeibank.identity.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ra.edu.rikkeibank.identity.entity.Role;
import ra.edu.rikkeibank.identity.entity.User;
import ra.edu.rikkeibank.identity.entity.UserStatus;
import ra.edu.rikkeibank.identity.repository.UserRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Seeding initial users for RikkeiBank Identity Service...");

            // 1. ADMIN
            userRepository.save(User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("Admin@123"))
                    .fullName("RikkeiBank System Administrator")
                    .email("admin@rikkeibank.com")
                    .phoneNumber("0901234567")
                    .role(Role.ROLE_ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build());

            // 2. TELLER HN
            userRepository.save(User.builder()
                    .username("teller_hn")
                    .password(passwordEncoder.encode("Teller@123"))
                    .fullName("Nguyen Van Teller - Ha Noi Branch")
                    .email("teller.hn@rikkeibank.com")
                    .phoneNumber("0912345678")
                    .role(Role.ROLE_TELLER)
                    .branchCode("BRANCH_HN")
                    .status(UserStatus.ACTIVE)
                    .build());

            // 3. TELLER HCM
            userRepository.save(User.builder()
                    .username("teller_hcm")
                    .password(passwordEncoder.encode("Teller@123"))
                    .fullName("Tran Thi Teller - TP Ho Chi Minh Branch")
                    .email("teller.hcm@rikkeibank.com")
                    .phoneNumber("0923456789")
                    .role(Role.ROLE_TELLER)
                    .branchCode("BRANCH_HCM")
                    .status(UserStatus.ACTIVE)
                    .build());

            // 4. CUSTOMER 1
            userRepository.save(User.builder()
                    .username("customer1")
                    .password(passwordEncoder.encode("Customer@123"))
                    .fullName("Le Van Khach Hang 1")
                    .email("customer1@gmail.com")
                    .phoneNumber("0934567890")
                    .role(Role.ROLE_CUSTOMER)
                    .customerCode("CUST001")
                    .status(UserStatus.ACTIVE)
                    .build());

            // 5. CUSTOMER 2
            userRepository.save(User.builder()
                    .username("customer2")
                    .password(passwordEncoder.encode("Customer@123"))
                    .fullName("Pham Thi Khach Hang 2")
                    .email("customer2@gmail.com")
                    .phoneNumber("0945678901")
                    .role(Role.ROLE_CUSTOMER)
                    .customerCode("CUST002")
                    .status(UserStatus.ACTIVE)
                    .build());

            log.info("RikkeiBank Identity users successfully seeded!");
        }
    }
}
