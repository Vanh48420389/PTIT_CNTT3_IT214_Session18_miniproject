package ra.edu.rikkeibank.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ra.edu.rikkeibank.customer.entity.CustomerStatus;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {
    @NotBlank(message = "Customer code cannot be blank")
    private String customerCode;

    @NotBlank(message = "Full name cannot be blank")
    private String fullName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Phone number cannot be blank")
    private String phoneNumber;

    @NotBlank(message = "Identity card number cannot be blank")
    private String identityCard;

    private String address;
    private LocalDate dateOfBirth;
    private CustomerStatus status;
    private String branchCode;
}
