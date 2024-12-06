package hexlet.code.page;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlDto {

    private final Long id;

    private final String name;

    private final LocalDateTime latestCheckCreatedAt;

    private final Integer latestCheckStatusCode;

    public static UrlDto toUrlDto(Url url, UrlCheck latestCheck) {
        return new UrlDto(
                url.getId(),
                url.getName(),
                latestCheck.getCreatedAt(),
                latestCheck.getStatusCode()
        );
    }
}
