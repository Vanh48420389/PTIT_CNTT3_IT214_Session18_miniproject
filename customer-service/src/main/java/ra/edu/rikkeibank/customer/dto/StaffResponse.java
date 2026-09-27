package ra.edu.rikkeibank.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ra.edu.rikkeibank.customer.entity.CustomerStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String staffCode;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
    private String branchCode;
    private CustomerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
