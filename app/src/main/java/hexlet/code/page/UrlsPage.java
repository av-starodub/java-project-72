package hexlet.code.page;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public final class UrlsPage extends BasePage {

    private final List<UrlDto> urlDtos;
}
