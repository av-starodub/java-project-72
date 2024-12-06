package hexlet.code.page;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class BasePage {

    private String flash;

    private String alertType;

    public final String formatTimestamp(LocalDateTime timestamp, String pattern) {
        return timestamp != null
                ? timestamp.format(DateTimeFormatter.ofPattern(pattern))
                : "";
    }
}
