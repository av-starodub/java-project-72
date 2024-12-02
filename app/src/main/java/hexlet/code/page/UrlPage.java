package hexlet.code.page;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public final class UrlPage extends BasePage {

    private Url url;

    private List<UrlCheck> urlChecks;
}
