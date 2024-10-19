package hexlet.code.page;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public final class UrlPage extends BasePage {

    private Url url;

    private List<UrlCheck> urlChecks;

    public String formatCreatedAt() {
        return url.getCreatedAt().toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy kk:mm"));
    }
}
