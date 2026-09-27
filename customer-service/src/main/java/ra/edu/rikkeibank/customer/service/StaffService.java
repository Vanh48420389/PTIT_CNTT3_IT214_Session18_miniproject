package ra.edu.rikkeibank.customer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ra.edu.rikkeibank.customer.dto.StaffRequest;
import ra.edu.rikkeibank.customer.dto.StaffResponse;
import ra.edu.rikkeibank.customer.entity.CustomerStatus;
import ra.edu.rikkeibank.customer.entity.Staff;
import ra.edu.rikkeibank.customer.exception.ApiException;
import ra.edu.rikkeibank.customer.repository.StaffRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffService {

    private final StaffRepository staffRepository;

    public List<StaffResponse> getAllStaff() {
        return staffRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "staff", key = "#id")
    public StaffResponse getStaffById(Long id) {
        log.info("Fetching staff from DB with ID: {}", id);
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Staff not found with id: " + id, "STAFF_NOT_FOUND"));
        return mapToResponse(staff);
    }

    public StaffResponse getStaffByCode(String staffCode) {
        Staff staff = staffRepository.findByStaffCode(staffCode)
                .orElseThrow(() -> ApiException.notFound("Staff not found with code: " + staffCode, "STAFF_NOT_FOUND"));
        return mapToResponse(staff);
    }

    @Transactional
    public StaffResponse createStaff(StaffRequest request) {
        if (staffRepository.existsByStaffCode(request.getStaffCode())) {
            throw ApiException.badRequest("Staff code already exists: " + request.getStaffCode(), "DUPLICATE_STAFF_CODE");
        }

        Staff staff = Staff.builder()
                .staffCode(request.getStaffCode())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .branchCode(request.getBranchCode())
                .status(request.getStatus() != null ? request.getStatus() : CustomerStatus.ACTIVE)
                .build();

        Staff saved = staffRepository.save(staff);
        return mapToResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "staff", key = "#id")
    public StaffResponse updateStaff(Long id, StaffRequest request) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Staff not found with id: " + id, "STAFF_NOT_FOUND"));

        staff.setFullName(request.getFullName());
        staff.setPhoneNumber(request.getPhoneNumber());
        staff.setEmail(request.getEmail());
        staff.setRole(request.getRole());
        staff.setBranchCode(request.getBranchCode());
        if (request.getStatus() != null) staff.setStatus(request.getStatus());

        Staff updated = staffRepository.save(staff);
        return mapToResponse(updated);
    }

    @Transactional
    @CacheEvict(value = "staff", key = "#id")
    public void deleteStaff(Long id) {
        if (!staffRepository.existsById(id)) {
            throw ApiException.notFound("Staff not found with id: " + id, "STAFF_NOT_FOUND");
        }
        staffRepository.deleteById(id);
    }

    public List<StaffResponse> getStaffByBranch(String branchCode) {
        return staffRepository.findByBranchCode(branchCode).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private StaffResponse mapToResponse(Staff staff) {
        return StaffResponse.builder()
                .id(staff.getId())
                .staffCode(staff.getStaffCode())
                .fullName(staff.getFullName())
                .email(staff.getEmail())
                .phoneNumber(staff.getPhoneNumber())
                .role(staff.getRole())
                .branchCode(staff.getBranchCode())
                .status(staff.getStatus())
                .createdAt(staff.getCreatedAt())
                .updatedAt(staff.getUpdatedAt())
                .build();
    }
}
