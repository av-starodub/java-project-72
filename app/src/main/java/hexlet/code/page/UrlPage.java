package hexlet.code.page;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public final class UrlPage extends BasePage {

    private final Url url;

    private final List<UrlCheck> urlChecks;
}
