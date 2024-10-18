package hexlet.code.repository.exception;

public class UrlDaoException extends RuntimeException {

    public UrlDaoException(String message) {
        this(message, null);
    }

    public UrlDaoException(String message, Throwable exception) {
        super(message, exception);
    }
}
