package ra.edu.rikkeibank.identity.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ra.edu.rikkeibank.identity.dto.ApiResponse;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiException(ApiException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("code", ex.getErrorCode());
        error.put("status", ex.getStatus().value());

        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), error);
        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> error = new HashMap<>();
        error.put("code", "VALIDATION_FAILED");
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("details", fieldErrors);

        ApiResponse<Object> response = ApiResponse.error("Input validation failed", error);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("code", "INTERNAL_SERVER_ERROR");
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.put("details", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error("An unexpected error occurred", error);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
