package ra.edu.rikkeibank.customer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ra.edu.rikkeibank.customer.dto.ApiResponse;
import ra.edu.rikkeibank.customer.dto.StaffRequest;
import ra.edu.rikkeibank.customer.dto.StaffResponse;
import ra.edu.rikkeibank.customer.exception.ApiException;
import ra.edu.rikkeibank.customer.service.StaffService;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getAllStaff(
            @RequestParam(required = false) String branchCode,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (userRole != null && !userRole.contains("ADMIN") && !userRole.contains("TELLER")) {
            throw ApiException.forbidden("Only staff and admins can view staff directory", "FORBIDDEN_ACCESS");
        }

        List<StaffResponse> list = (branchCode != null && !branchCode.isBlank())
                ? staffService.getStaffByBranch(branchCode)
                : staffService.getAllStaff();

        return ResponseEntity.ok(ApiResponse.success(list, "Retrieved staff directory"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffResponse>> getStaffById(@PathVariable Long id) {
        StaffResponse staff = staffService.getStaffById(id);
        return ResponseEntity.ok(ApiResponse.success(staff, "Retrieved staff details"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StaffResponse>> createStaff(
            @Valid @RequestBody StaffRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (userRole != null && !userRole.contains("ADMIN")) {
            throw ApiException.forbidden("Only ADMIN can register new staff", "FORBIDDEN_ADMIN_ONLY");
        }
        StaffResponse created = staffService.createStaff(request);
        return new ResponseEntity<>(ApiResponse.success(created, "Staff created successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffResponse>> updateStaff(
            @PathVariable Long id,
            @Valid @RequestBody StaffRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (userRole != null && !userRole.contains("ADMIN")) {
            throw ApiException.forbidden("Only ADMIN can update staff information", "FORBIDDEN_ADMIN_ONLY");
        }
        StaffResponse updated = staffService.updateStaff(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Staff updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (userRole != null && !userRole.contains("ADMIN")) {
            throw ApiException.forbidden("Only ADMIN can remove staff", "FORBIDDEN_ADMIN_ONLY");
        }
        staffService.deleteStaff(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Staff deleted successfully"));
    }
}
