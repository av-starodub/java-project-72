package hexlet.code.page;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
public class BasePage {
    private String flash;
    private String alertType;
    private List<Error> errors;

    public final String formatTimestamp(Timestamp timestamp, String pattern) {
        return timestamp != null
                ? timestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern(pattern))
                : "";
    }
}
