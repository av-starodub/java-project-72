package hexlet.code.repository.exception;

public class UrlCheckDaoException extends RuntimeException {

    public UrlCheckDaoException(String message) {
        this(message, null);
    }

    public UrlCheckDaoException(String message, Throwable exception) {
        super(message, exception);
    }
}
