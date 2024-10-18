package hexlet.code.repository.exception;

public class DuplicateUrlException extends RuntimeException {

    public DuplicateUrlException(String message) {
        this(message, null);
    }

    public DuplicateUrlException(String message, Throwable exception) {
        super(message, exception);
    }
}
