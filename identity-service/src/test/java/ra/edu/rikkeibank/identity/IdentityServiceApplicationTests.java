package ra.edu.rikkeibank.identity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ra.edu.rikkeibank.identity.dto.AuthResponse;
import ra.edu.rikkeibank.identity.dto.LoginRequest;
import ra.edu.rikkeibank.identity.service.AuthService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IdentityServiceApplicationTests {

    @Autowired
    private AuthService authService;

    @Test
    void contextLoads() {
        assertNotNull(authService);
    }

    @Test
    void testAdminLoginSuccess() {
        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("Admin@123")
                .build();
        AuthResponse response = authService.login(request);
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("admin", response.getUsername());
    }
}
