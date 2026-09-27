package ra.edu.rikkeibank.customer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ra.edu.rikkeibank.customer.dto.CustomerRequest;
import ra.edu.rikkeibank.customer.dto.CustomerResponse;
import ra.edu.rikkeibank.customer.entity.Customer;
import ra.edu.rikkeibank.customer.entity.CustomerStatus;
import ra.edu.rikkeibank.customer.exception.ApiException;
import ra.edu.rikkeibank.customer.repository.CustomerRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "customers", key = "#id")
    public CustomerResponse getCustomerById(Long id) {
        log.info("Fetching customer from database with ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Customer not found with id: " + id, "CUSTOMER_NOT_FOUND"));
        return mapToResponse(customer);
    }

    public CustomerResponse getCustomerByCode(String customerCode) {
        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> ApiException.notFound("Customer not found with code: " + customerCode, "CUSTOMER_NOT_FOUND"));
        return mapToResponse(customer);
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        if (customerRepository.existsByCustomerCode(request.getCustomerCode())) {
            throw ApiException.badRequest("Customer code already exists: " + request.getCustomerCode(), "DUPLICATE_CUSTOMER_CODE");
        }
        if (customerRepository.existsByIdentityCard(request.getIdentityCard())) {
            throw ApiException.badRequest("Identity card already exists: " + request.getIdentityCard(), "DUPLICATE_IDENTITY_CARD");
        }
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw ApiException.badRequest("Email already exists: " + request.getEmail(), "DUPLICATE_EMAIL");
        }

        Customer customer = Customer.builder()
                .customerCode(request.getCustomerCode())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .identityCard(request.getIdentityCard())
                .address(request.getAddress())
                .dateOfBirth(request.getDateOfBirth())
                .branchCode(request.getBranchCode())
                .status(request.getStatus() != null ? request.getStatus() : CustomerStatus.ACTIVE)
                .build();

        Customer saved = customerRepository.save(customer);
        return mapToResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "customers", key = "#id")
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        log.info("Updating customer and evicting cache for ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Customer not found with id: " + id, "CUSTOMER_NOT_FOUND"));

        customer.setFullName(request.getFullName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());
        customer.setDateOfBirth(request.getDateOfBirth());
        if (request.getBranchCode() != null) customer.setBranchCode(request.getBranchCode());
        if (request.getStatus() != null) customer.setStatus(request.getStatus());

        Customer updated = customerRepository.save(customer);
        return mapToResponse(updated);
    }

    @Transactional
    @CacheEvict(value = "customers", key = "#id")
    public void deleteCustomer(Long id) {
        log.info("Deleting customer and evicting cache for ID: {}", id);
        if (!customerRepository.existsById(id)) {
            throw ApiException.notFound("Customer not found with id: " + id, "CUSTOMER_NOT_FOUND");
        }
        customerRepository.deleteById(id);
    }

    public List<CustomerResponse> getCustomersByBranch(String branchCode) {
        return customerRepository.findByBranchCode(branchCode).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .customerCode(customer.getCustomerCode())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .identityCard(customer.getIdentityCard())
                .address(customer.getAddress())
                .dateOfBirth(customer.getDateOfBirth())
                .status(customer.getStatus())
                .branchCode(customer.getBranchCode())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
