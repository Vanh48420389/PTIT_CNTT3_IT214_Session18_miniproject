package ra.edu.rikkeibank.customer.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public ApiException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public static ApiException badRequest(String message, String errorCode) {
        return new ApiException(message, HttpStatus.BAD_REQUEST, errorCode);
    }

    public static ApiException notFound(String message, String errorCode) {
        return new ApiException(message, HttpStatus.NOT_FOUND, errorCode);
    }

    public static ApiException forbidden(String message, String errorCode) {
        return new ApiException(message, HttpStatus.FORBIDDEN, errorCode);
    }
}
