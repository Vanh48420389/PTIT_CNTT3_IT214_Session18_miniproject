package ra.edu.rikkeibank.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidateResponse {
    private boolean valid;
    private String username;
    private String role;
    private String branchCode;
    private Long userId;
    private String message;
}
