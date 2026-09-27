package ra.edu.rikkeibank.identity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ra.edu.rikkeibank.identity.dto.*;
import ra.edu.rikkeibank.identity.entity.Role;
import ra.edu.rikkeibank.identity.entity.User;
import ra.edu.rikkeibank.identity.entity.UserStatus;
import ra.edu.rikkeibank.identity.exception.ApiException;
import ra.edu.rikkeibank.identity.repository.UserRepository;
import ra.edu.rikkeibank.identity.util.JwtTokenProvider;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    private static final String BLACKLIST_KEY_PREFIX = "token:blacklist:";
    private static final String USER_REVOKE_KEY_PREFIX = "user:revoked:";

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> ApiException.unauthorized("Invalid username or password", "AUTH_INVALID_CREDENTIALS"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw ApiException.forbidden("User account is " + user.getStatus(), "AUTH_ACCOUNT_DISABLED");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw ApiException.unauthorized("Invalid username or password", "AUTH_INVALID_CREDENTIALS");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole())
                .branchCode(user.getBranchCode())
                .customerCode(user.getCustomerCode())
                .build();
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw ApiException.badRequest("Username already exists", "USERNAME_ALREADY_EXISTS");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApiException.badRequest("Email already exists", "EMAIL_ALREADY_EXISTS");
        }

        Role role = request.getRole() != null ? request.getRole() : Role.ROLE_CUSTOMER;

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .role(role)
                .status(UserStatus.ACTIVE)
                .branchCode(request.getBranchCode())
                .customerCode(request.getCustomerCode())
                .build();

        User saved = userRepository.save(user);
        return mapToUserResponse(saved);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw ApiException.unauthorized("Invalid or expired refresh token", "INVALID_REFRESH_TOKEN");
        }

        String username = jwtTokenProvider.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> ApiException.unauthorized("User not found", "USER_NOT_FOUND"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw ApiException.forbidden("User account is " + user.getStatus(), "AUTH_ACCOUNT_DISABLED");
        }

        // Check if user was revoked in Redis
        if (isUserRevoked(username)) {
            throw ApiException.unauthorized("Session revoked by administrator", "SESSION_REVOKED");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole())
                .branchCode(user.getBranchCode())
                .customerCode(user.getCustomerCode())
                .build();
    }

    @Transactional
    public void forceLogout(Long targetUserId) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> ApiException.notFound("User not found with id: " + targetUserId, "USER_NOT_FOUND"));

        // Revoke user in Redis with TTL (e.g. 30 days)
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(USER_REVOKE_KEY_PREFIX + user.getUsername(), "REVOKED", 30, TimeUnit.DAYS);
                log.info("User {} revoked in Redis blacklist successfully", user.getUsername());
            } catch (Exception e) {
                log.warn("Redis not accessible for force-logout blacklist, updating database state directly: {}", e.getMessage());
            }
        }

        // Also update DB status to ensure persistence
        user.setStatus(UserStatus.REVOKED);
        userRepository.save(user);
        log.info("Administrator force-logged out user ID {}", targetUserId);
    }

    public TokenValidateResponse validateToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            return TokenValidateResponse.builder()
                    .valid(false)
                    .message("Token signature or expiry invalid")
                    .build();
        }

        String username = jwtTokenProvider.extractUsername(token);
        if (isUserRevoked(username)) {
            return TokenValidateResponse.builder()
                    .valid(false)
                    .message("Token revoked by administrator")
                    .build();
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            return TokenValidateResponse.builder()
                    .valid(false)
                    .message("User inactive or revoked")
                    .build();
        }

        return TokenValidateResponse.builder()
                .valid(true)
                .username(user.getUsername())
                .role(user.getRole().name())
                .branchCode(user.getBranchCode())
                .userId(user.getId())
                .message("Token valid")
                .build();
    }

    private boolean isUserRevoked(String username) {
        if (redisTemplate != null) {
            try {
                Boolean hasKey = redisTemplate.hasKey(USER_REVOKE_KEY_PREFIX + username);
                return Boolean.TRUE.equals(hasKey);
            } catch (Exception e) {
                log.warn("Redis check failed: {}", e.getMessage());
            }
        }
        return false;
    }

    public UserResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> ApiException.notFound("User not found: " + username, "USER_NOT_FOUND"));
        return mapToUserResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .branchCode(user.getBranchCode())
                .customerCode(user.getCustomerCode())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
