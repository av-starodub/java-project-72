package hexlet.code.page;

import hexlet.code.model.Url;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@AllArgsConstructor
public final class UrlPage extends BasePage {
    private Url url;

    public String formatCreatedAt() {
        return url.getCreatedAt().toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy kk:mm"));
    }
}
