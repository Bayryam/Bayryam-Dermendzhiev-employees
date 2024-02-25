package bg.sirma.problem.exception;

public class InvalidRowLengthException extends Exception {
    public InvalidRowLengthException(String message) {
        super(message);
    }

    public InvalidRowLengthException(String message, Throwable cause) {
        super(message, cause);
    }
}
