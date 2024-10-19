package hexlet.code.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlCheck {
    private Long id;
    private Long urlId;
    private Integer statusCode;
    private String title;
    private String h1;
    private String description;
    private Timestamp createdAt;

    public final String formatCreatedAt() {
        return createdAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy kk:mm"));
    }
}
