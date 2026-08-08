package re.pronunciationservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Xử lý tập trung tất cả các exception trong pronunciation-service.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý lỗi validate request (@Valid).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: {}", errors);
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error", errors);
    }

    /**
     * Xử lý request không hợp lệ (audioId / transcript rỗng, v.v.).
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRequest(InvalidRequestException ex) {
        log.warn("Invalid request: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    /**
     * Bắt tất cả exception không xác định khác.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
