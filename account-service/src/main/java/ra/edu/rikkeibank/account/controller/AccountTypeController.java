package ra.edu.rikkeibank.account.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ra.edu.rikkeibank.account.dto.AccountTypeRequest;
import ra.edu.rikkeibank.account.dto.AccountTypeResponse;
import ra.edu.rikkeibank.account.dto.ApiResponse;
import ra.edu.rikkeibank.account.exception.ApiException;
import ra.edu.rikkeibank.account.service.AccountTypeService;

import java.util.List;

@RestController
@RequestMapping("/api/account-types")
@RequiredArgsConstructor
public class AccountTypeController {

    private final AccountTypeService accountTypeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountTypeResponse>>> getAllAccountTypes() {
        List<AccountTypeResponse> types = accountTypeService.getAllAccountTypes();
        return ResponseEntity.ok(ApiResponse.success(types, "Retrieved account types"));
    }

    @GetMapping("/{typeCode}")
    public ResponseEntity<ApiResponse<AccountTypeResponse>> getAccountType(@PathVariable String typeCode) {
        AccountTypeResponse type = accountTypeService.getAccountTypeByCode(typeCode);
        return ResponseEntity.ok(ApiResponse.success(type, "Retrieved account type details"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountTypeResponse>> createAccountType(
            @Valid @RequestBody AccountTypeRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (userRole != null && !userRole.contains("ADMIN")) {
            throw ApiException.forbidden("Only ADMIN can create account types", "FORBIDDEN_ADMIN_ONLY");
        }
        AccountTypeResponse created = accountTypeService.createAccountType(request);
        return new ResponseEntity<>(ApiResponse.success(created, "Account type created"), HttpStatus.CREATED);
    }

    @PutMapping("/{typeCode}")
    public ResponseEntity<ApiResponse<AccountTypeResponse>> updateAccountType(
            @PathVariable String typeCode,
            @Valid @RequestBody AccountTypeRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (userRole != null && !userRole.contains("ADMIN")) {
            throw ApiException.forbidden("Only ADMIN can update account types", "FORBIDDEN_ADMIN_ONLY");
        }
        AccountTypeResponse updated = accountTypeService.updateAccountType(typeCode, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Account type updated"));
    }

    @DeleteMapping("/{typeCode}")
    public ResponseEntity<ApiResponse<Void>> deleteAccountType(
            @PathVariable String typeCode,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (userRole != null && !userRole.contains("ADMIN")) {
            throw ApiException.forbidden("Only ADMIN can delete account types", "FORBIDDEN_ADMIN_ONLY");
        }
        accountTypeService.deleteAccountType(typeCode);
        return ResponseEntity.ok(ApiResponse.success(null, "Account type deleted"));
    }
}
