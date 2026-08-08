package re.aianalysisservice.exception;

public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
