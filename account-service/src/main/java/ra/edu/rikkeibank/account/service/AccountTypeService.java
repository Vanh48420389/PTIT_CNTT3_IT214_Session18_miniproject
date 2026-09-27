package ra.edu.rikkeibank.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ra.edu.rikkeibank.account.dto.AccountTypeRequest;
import ra.edu.rikkeibank.account.dto.AccountTypeResponse;
import ra.edu.rikkeibank.account.entity.AccountStatus;
import ra.edu.rikkeibank.account.entity.AccountType;
import ra.edu.rikkeibank.account.exception.ApiException;
import ra.edu.rikkeibank.account.repository.AccountTypeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountTypeService {

    private final AccountTypeRepository accountTypeRepository;

    public List<AccountTypeResponse> getAllAccountTypes() {
        return accountTypeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "accountTypes", key = "#typeCode")
    public AccountTypeResponse getAccountTypeByCode(String typeCode) {
        log.info("Fetching account type from DB: {}", typeCode);
        AccountType type = accountTypeRepository.findByTypeCode(typeCode)
                .orElseThrow(() -> ApiException.notFound("Account type not found: " + typeCode, "ACCOUNT_TYPE_NOT_FOUND"));
        return mapToResponse(type);
    }

    @Transactional
    public AccountTypeResponse createAccountType(AccountTypeRequest request) {
        if (accountTypeRepository.existsByTypeCode(request.getTypeCode())) {
            throw ApiException.badRequest("Account type code already exists: " + request.getTypeCode(), "DUPLICATE_TYPE_CODE");
        }

        AccountType type = AccountType.builder()
                .typeCode(request.getTypeCode())
                .typeName(request.getTypeName())
                .description(request.getDescription())
                .interestRate(request.getInterestRate())
                .minBalance(request.getMinBalance())
                .dailyTransferLimit(request.getDailyTransferLimit())
                .currency(request.getCurrency() != null ? request.getCurrency() : "VND")
                .status(request.getStatus() != null ? request.getStatus() : AccountStatus.ACTIVE)
                .build();

        AccountType saved = accountTypeRepository.save(type);
        return mapToResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "accountTypes", key = "#typeCode")
    public AccountTypeResponse updateAccountType(String typeCode, AccountTypeRequest request) {
        AccountType type = accountTypeRepository.findByTypeCode(typeCode)
                .orElseThrow(() -> ApiException.notFound("Account type not found: " + typeCode, "ACCOUNT_TYPE_NOT_FOUND"));

        type.setTypeName(request.getTypeName());
        type.setDescription(request.getDescription());
        type.setInterestRate(request.getInterestRate());
        type.setMinBalance(request.getMinBalance());
        type.setDailyTransferLimit(request.getDailyTransferLimit());
        if (request.getStatus() != null) type.setStatus(request.getStatus());

        AccountType updated = accountTypeRepository.save(type);
        return mapToResponse(updated);
    }

    @Transactional
    @CacheEvict(value = "accountTypes", key = "#typeCode")
    public void deleteAccountType(String typeCode) {
        AccountType type = accountTypeRepository.findByTypeCode(typeCode)
                .orElseThrow(() -> ApiException.notFound("Account type not found: " + typeCode, "ACCOUNT_TYPE_NOT_FOUND"));
        accountTypeRepository.delete(type);
    }

    private AccountTypeResponse mapToResponse(AccountType type) {
        return AccountTypeResponse.builder()
                .id(type.getId())
                .typeCode(type.getTypeCode())
                .typeName(type.getTypeName())
                .description(type.getDescription())
                .interestRate(type.getInterestRate())
                .minBalance(type.getMinBalance())
                .dailyTransferLimit(type.getDailyTransferLimit())
                .currency(type.getCurrency())
                .status(type.getStatus())
                .createdAt(type.getCreatedAt())
                .build();
    }
}
