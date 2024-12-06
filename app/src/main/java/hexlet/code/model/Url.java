package hexlet.code.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.NonNull;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class Url {

    private long id;

    @NonNull
    private final String name;

    private LocalDateTime createdAt;
}
