package ra.edu.rikkeibank.identity.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ra.edu.rikkeibank.identity.dto.*;
import ra.edu.rikkeibank.identity.exception.ApiException;
import ra.edu.rikkeibank.identity.service.AuthService;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return new ResponseEntity<>(ApiResponse.success(response, "User registered successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<TokenValidateResponse>> validateToken(@RequestParam String token) {
        TokenValidateResponse response = authService.validateToken(token);
        return ResponseEntity.ok(ApiResponse.success(response, "Token validation completed"));
    }

    @PostMapping("/force-logout/{userId}")
    public ResponseEntity<ApiResponse<Void>> forceLogout(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        // Enforce ADMIN permission
        if (userRole != null && !userRole.contains("ADMIN")) {
            throw ApiException.forbidden("Only ADMIN can force logout users", "FORBIDDEN_ADMIN_ONLY");
        }

        authService.forceLogout(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "User ID " + userId + " has been forcefully logged out and tokens revoked"));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (userRole != null && !userRole.contains("ADMIN")) {
            throw ApiException.forbidden("Only ADMIN can view all users", "FORBIDDEN_ADMIN_ONLY");
        }
        List<UserResponse> users = authService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Retrieved all users"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @RequestHeader(value = "X-User-Name", required = false) String username) {
        if (username == null || username.isBlank()) {
            throw ApiException.unauthorized("Authentication required", "UNAUTHORIZED");
        }
        UserResponse user = authService.getUserProfile(username);
        return ResponseEntity.ok(ApiResponse.success(user, "Retrieved user profile"));
    }
}
