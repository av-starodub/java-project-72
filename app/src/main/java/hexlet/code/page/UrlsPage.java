package hexlet.code.page;

import hexlet.code.model.Url;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public final class UrlsPage extends BasePage {
    private final List<Url> urls;
}
