package hexlet.code.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.NonNull;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class Url {

    private long id;

    @NonNull
    private final String name;

    private Timestamp createdAt;
}
