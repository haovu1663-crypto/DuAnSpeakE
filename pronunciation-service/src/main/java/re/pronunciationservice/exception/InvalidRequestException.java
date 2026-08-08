package re.pronunciationservice.exception;

/**
 * Ném ra khi request không hợp lệ (ví dụ: audioId hoặc transcript rỗng,
 * hoặc không thể đọc file audio).
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
