package ra.edu.rikkeibank.account.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ra.edu.rikkeibank.account.entity.Account;
import ra.edu.rikkeibank.account.entity.AccountStatus;
import ra.edu.rikkeibank.account.entity.AccountType;
import ra.edu.rikkeibank.account.repository.AccountRepository;
import ra.edu.rikkeibank.account.repository.AccountTypeRepository;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountDataInitializer implements CommandLineRunner {

    private final AccountTypeRepository accountTypeRepository;
    private final AccountRepository accountRepository;

    @Override
    public void run(String... args) {
        if (accountTypeRepository.count() == 0) {
            log.info("Seeding account types...");

            accountTypeRepository.save(AccountType.builder()
                    .typeCode("CHECKING")
                    .typeName("Tài khoản thanh toán cá nhân")
                    .description("Thanh toán, chuyển khoản, rút tiền 24/7 linh hoạt")
                    .interestRate(0.002) // 0.2%/year
                    .minBalance(new BigDecimal("50000"))
                    .dailyTransferLimit(new BigDecimal("500000000"))
                    .currency("VND")
                    .status(AccountStatus.ACTIVE)
                    .build());

            accountTypeRepository.save(AccountType.builder()
                    .typeCode("SAVINGS")
                    .typeName("Tài khoản tiền gửi tiết kiệm")
                    .description("Tiết kiệm sinh lời cao với lãi suất cạnh tranh")
                    .interestRate(0.065) // 6.5%/year
                    .minBalance(new BigDecimal("1000000"))
                    .dailyTransferLimit(new BigDecimal("100000000"))
                    .currency("VND")
                    .status(AccountStatus.ACTIVE)
                    .build());

            accountTypeRepository.save(AccountType.builder()
                    .typeCode("SALARY")
                    .typeName("Tài khoản nhận lương doanh nghiệp")
                    .description("Miễn phí chuyển khoản, ưu đãi thấu chi")
                    .interestRate(0.005)
                    .minBalance(new BigDecimal("0"))
                    .dailyTransferLimit(new BigDecimal("200000000"))
                    .currency("VND")
                    .status(AccountStatus.ACTIVE)
                    .build());
        }

        if (accountRepository.count() == 0) {
            log.info("Seeding initial bank accounts...");

            accountRepository.save(Account.builder()
                    .accountNumber("1000000001")
                    .customerCode("CUST001")
                    .accountTypeCode("CHECKING")
                    .balance(new BigDecimal("50000000")) // 50,000,000 VND
                    .currency("VND")
                    .branchCode("BRANCH_HN")
                    .status(AccountStatus.ACTIVE)
                    .build());

            accountRepository.save(Account.builder()
                    .accountNumber("1000000002")
                    .customerCode("CUST002")
                    .accountTypeCode("CHECKING")
                    .balance(new BigDecimal("20000000")) // 20,000,000 VND
                    .currency("VND")
                    .branchCode("BRANCH_HCM")
                    .status(AccountStatus.ACTIVE)
                    .build());

            accountRepository.save(Account.builder()
                    .accountNumber("1000000003")
                    .customerCode("CUST001")
                    .accountTypeCode("SAVINGS")
                    .balance(new BigDecimal("100000000")) // 100,000,000 VND
                    .currency("VND")
                    .branchCode("BRANCH_HN")
                    .status(AccountStatus.ACTIVE)
                    .build());

            log.info("Bank accounts seeded successfully!");
        }
    }
}
