package hexlet.code.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Url {
    private long id;
    private String name;
    private Timestamp createdAt;

    public Url(long urlId, String url) {
        id = urlId;
        name = url;
    }
}
