package hexlet.code.repository.exception;

public class DataBaseOperationException extends RuntimeException {

    public DataBaseOperationException(String massage, Throwable cause) {
        super(massage, cause);
    }

    public DataBaseOperationException(String massage) {
        this(massage, null);
    }
}
