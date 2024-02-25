package bg.sirma.problem.exception;

public class UnsupportedDateFormatException extends Exception {
    public UnsupportedDateFormatException(String message) {
        super(message);
    }

    public UnsupportedDateFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
