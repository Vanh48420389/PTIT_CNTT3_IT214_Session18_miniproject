package ra.edu.rikkeibank.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ra.edu.rikkeibank.customer.entity.CustomerStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffRequest {
    @NotBlank(message = "Staff code cannot be blank")
    private String staffCode;

    @NotBlank(message = "Full name cannot be blank")
    private String fullName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Phone number cannot be blank")
    private String phoneNumber;

    @NotBlank(message = "Role cannot be blank (e.g. TELLER, MANAGER)")
    private String role;

    @NotBlank(message = "Branch code cannot be blank (e.g. BRANCH_HN, BRANCH_HCM)")
    private String branchCode;

    private CustomerStatus status;
}
