package hexlet.code.datasource;

public class DatabasePropertiesReadException extends RuntimeException {

    public DatabasePropertiesReadException(String massage, Throwable cause) {
        super(massage, cause);
    }

}
