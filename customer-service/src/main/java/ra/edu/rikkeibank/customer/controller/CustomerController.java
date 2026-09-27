package ra.edu.rikkeibank.customer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ra.edu.rikkeibank.customer.dto.ApiResponse;
import ra.edu.rikkeibank.customer.dto.CustomerRequest;
import ra.edu.rikkeibank.customer.dto.CustomerResponse;
import ra.edu.rikkeibank.customer.exception.ApiException;
import ra.edu.rikkeibank.customer.service.CustomerService;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers(
            @RequestParam(required = false) String branchCode,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        // ADMIN and TELLER can view customers list
        if (userRole != null && userRole.contains("CUSTOMER")) {
            throw ApiException.forbidden("Customers can only view their own profile", "FORBIDDEN_ACCESS");
        }

        List<CustomerResponse> list = (branchCode != null && !branchCode.isBlank())
                ? customerService.getCustomersByBranch(branchCode)
                : customerService.getAllCustomers();

        return ResponseEntity.ok(ApiResponse.success(list, "Retrieved customers list"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable Long id) {
        CustomerResponse customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(customer, "Retrieved customer details"));
    }

    @GetMapping("/code/{customerCode}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerByCode(@PathVariable String customerCode) {
        CustomerResponse customer = customerService.getCustomerByCode(customerCode);
        return ResponseEntity.ok(ApiResponse.success(customer, "Retrieved customer details"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CustomerRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (userRole != null && !userRole.contains("ADMIN")) {
            throw ApiException.forbidden("Only ADMIN can create new customers", "FORBIDDEN_ADMIN_ONLY");
        }
        CustomerResponse created = customerService.createCustomer(request);
        return new ResponseEntity<>(ApiResponse.success(created, "Customer created successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (userRole != null && !userRole.contains("ADMIN")) {
            throw ApiException.forbidden("Only ADMIN can update customer profiles", "FORBIDDEN_ADMIN_ONLY");
        }
        CustomerResponse updated = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Customer updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (userRole != null && !userRole.contains("ADMIN")) {
            throw ApiException.forbidden("Only ADMIN can delete customers", "FORBIDDEN_ADMIN_ONLY");
        }
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Customer deleted successfully"));
    }
}
