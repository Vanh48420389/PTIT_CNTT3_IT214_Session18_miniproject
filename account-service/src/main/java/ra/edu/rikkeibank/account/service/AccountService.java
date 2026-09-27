package ra.edu.rikkeibank.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ra.edu.rikkeibank.account.dto.AccountRequest;
import ra.edu.rikkeibank.account.dto.AccountResponse;
import ra.edu.rikkeibank.account.dto.BalanceOperationRequest;
import ra.edu.rikkeibank.account.dto.BalanceOperationResponse;
import ra.edu.rikkeibank.account.entity.Account;
import ra.edu.rikkeibank.account.entity.AccountStatus;
import ra.edu.rikkeibank.account.exception.ApiException;
import ra.edu.rikkeibank.account.repository.AccountRepository;
import ra.edu.rikkeibank.account.repository.AccountTypeRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountTypeRepository accountTypeRepository;

    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "accounts", key = "#accountNumber")
    public AccountResponse getAccountByNumber(String accountNumber) {
        log.info("Cache miss or DB fetch for account: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> ApiException.notFound("Account not found: " + accountNumber, "ACCOUNT_NOT_FOUND"));
        return mapToResponse(account);
    }

    public List<AccountResponse> getAccountsByCustomerCode(String customerCode) {
        return accountRepository.findByCustomerCode(customerCode).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AccountResponse> getAccountsByBranch(String branchCode) {
        return accountRepository.findByBranchCode(branchCode).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        if (accountRepository.existsByAccountNumber(request.getAccountNumber())) {
            throw ApiException.badRequest("Account number already exists: " + request.getAccountNumber(), "DUPLICATE_ACCOUNT_NUMBER");
        }

        if (!accountTypeRepository.existsByTypeCode(request.getAccountTypeCode())) {
            throw ApiException.badRequest("Invalid account type: " + request.getAccountTypeCode(), "INVALID_ACCOUNT_TYPE");
        }

        Account account = Account.builder()
                .accountNumber(request.getAccountNumber())
                .customerCode(request.getCustomerCode())
                .accountTypeCode(request.getAccountTypeCode())
                .balance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
                .currency(request.getCurrency() != null ? request.getCurrency() : "VND")
                .branchCode(request.getBranchCode() != null ? request.getBranchCode() : "BRANCH_HN")
                .status(request.getStatus() != null ? request.getStatus() : AccountStatus.ACTIVE)
                .build();

        Account saved = accountRepository.save(account);
        return mapToResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "accounts", key = "#accountNumber")
    public BalanceOperationResponse debit(String accountNumber, BalanceOperationRequest request) {
        log.info("Debiting account {} amount: {}", accountNumber, request.getAmount());
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> ApiException.notFound("Source account not found: " + accountNumber, "ACCOUNT_NOT_FOUND"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw ApiException.badRequest("Source account is " + account.getStatus() + ", cannot execute debit", "ACCOUNT_NOT_ACTIVE");
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw ApiException.badRequest("Insufficient funds. Current balance: " + account.getBalance() + ", Requested: " + request.getAmount(), "INSUFFICIENT_FUNDS");
        }

        BigDecimal previousBalance = account.getBalance();
        BigDecimal newBalance = previousBalance.subtract(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        return BalanceOperationResponse.builder()
                .accountNumber(accountNumber)
                .previousBalance(previousBalance)
                .amount(request.getAmount())
                .currentBalance(newBalance)
                .operationType("DEBIT")
                .transactionRef(request.getTransactionRef())
                .build();
    }

    @Transactional
    @CacheEvict(value = "accounts", key = "#accountNumber")
    public BalanceOperationResponse credit(String accountNumber, BalanceOperationRequest request) {
        log.info("Crediting account {} amount: {}", accountNumber, request.getAmount());
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> ApiException.notFound("Destination account not found: " + accountNumber, "ACCOUNT_NOT_FOUND"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw ApiException.badRequest("Destination account is " + account.getStatus() + ", cannot accept deposit", "ACCOUNT_NOT_ACTIVE");
        }

        BigDecimal previousBalance = account.getBalance();
        BigDecimal newBalance = previousBalance.add(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        return BalanceOperationResponse.builder()
                .accountNumber(accountNumber)
                .previousBalance(previousBalance)
                .amount(request.getAmount())
                .currentBalance(newBalance)
                .operationType("CREDIT")
                .transactionRef(request.getTransactionRef())
                .build();
    }

    @Transactional
    @CacheEvict(value = "accounts", key = "#accountNumber")
    public BalanceOperationResponse compensateCredit(String accountNumber, BalanceOperationRequest request) {
        log.info("Saga Compensating rollback: Refund to account {} amount: {}", accountNumber, request.getAmount());
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> ApiException.notFound("Account not found for compensation: " + accountNumber, "ACCOUNT_NOT_FOUND"));

        BigDecimal previousBalance = account.getBalance();
        BigDecimal newBalance = previousBalance.add(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        return BalanceOperationResponse.builder()
                .accountNumber(accountNumber)
                .previousBalance(previousBalance)
                .amount(request.getAmount())
                .currentBalance(newBalance)
                .operationType("COMPENSATE")
                .transactionRef(request.getTransactionRef())
                .build();
    }

    @Transactional
    @CacheEvict(value = "accounts", key = "#accountNumber")
    public AccountResponse updateStatus(String accountNumber, AccountStatus newStatus) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> ApiException.notFound("Account not found: " + accountNumber, "ACCOUNT_NOT_FOUND"));
        account.setStatus(newStatus);
        Account saved = accountRepository.save(account);
        return mapToResponse(saved);
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .customerCode(account.getCustomerCode())
                .accountTypeCode(account.getAccountTypeCode())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .branchCode(account.getBranchCode())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
