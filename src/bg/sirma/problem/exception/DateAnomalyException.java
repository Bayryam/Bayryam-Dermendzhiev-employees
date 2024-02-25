package bg.sirma.problem.exception;

public class DateAnomalyException extends Exception {
    public DateAnomalyException(String message) {
        super(message);
    }

    public DateAnomalyException(String message, Throwable cause) {
        super(message, cause);
    }
}
