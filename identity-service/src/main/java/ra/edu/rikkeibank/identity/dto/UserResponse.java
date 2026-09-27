package ra.edu.rikkeibank.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ra.edu.rikkeibank.identity.entity.Role;
import ra.edu.rikkeibank.identity.entity.UserStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Role role;
    private UserStatus status;
    private String branchCode;
    private String customerCode;
    private LocalDateTime createdAt;
}
