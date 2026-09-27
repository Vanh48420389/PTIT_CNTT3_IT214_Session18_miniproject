package ra.edu.rikkeibank.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import ra.edu.rikkeibank.gateway.util.JwtUtil;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<String> OPEN_API_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/validate",
            "/actuator",
            "/h2-console"
    );

    public AuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // Allow public paths
            boolean isOpenApi = OPEN_API_ENDPOINTS.stream().anyMatch(path::startsWith);
            if (isOpenApi) {
                return chain.filter(exchange);
            }

            // Check Authorization Header
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing Authorization Header. Authentication required.", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization Header format. Expected Bearer token.", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.isTokenValid(token)) {
                return onError(exchange, "JWT token is invalid, expired, or revoked.", HttpStatus.UNAUTHORIZED);
            }

            try {
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);
                String branch = jwtUtil.extractBranch(token);
                Long userId = jwtUtil.extractUserId(token);

                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Name", username != null ? username : "")
                        .header("X-User-Role", role != null ? role : "")
                        .header("X-User-Branch", branch != null ? branch : "")
                        .header("X-User-Id", userId != null ? String.valueOf(userId) : "")
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            } catch (Exception e) {
                return onError(exchange, "Failed to authenticate request: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String errMessage, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("success", false);
        errorDetails.put("status", httpStatus.value());
        errorDetails.put("message", errMessage);
        errorDetails.put("timestamp", LocalDateTime.now().toString());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsString(errorDetails).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            bytes = ("{\"success\":false,\"message\":\"" + errMessage + "\"}").getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // Configuration properties if needed
    }
}
