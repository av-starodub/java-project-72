package hexlet.code.repository.exception;

public class DuplicateEntityException extends RuntimeException {

    public DuplicateEntityException(String message) {
        this(message, null);
    }

    public DuplicateEntityException(String message, Throwable exception) {
        super(message, exception);
    }
}
