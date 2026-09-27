package ra.edu.rikkeibank.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ra.edu.rikkeibank.identity.entity.Role;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    private String password;

    @NotBlank(message = "Full name cannot be blank")
    private String fullName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    private String email;

    private String phoneNumber;

    private Role role; // Defaults to ROLE_CUSTOMER if null

    private String branchCode; // For TELLER

    private String customerCode; // For CUSTOMER
}
