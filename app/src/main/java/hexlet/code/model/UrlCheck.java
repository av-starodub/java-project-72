package hexlet.code.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Getter
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
}
