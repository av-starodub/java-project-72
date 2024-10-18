package hexlet.code;

import io.javalin.Javalin;
import io.javalin.http.NotFoundResponse;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class AppTest {
    private static final int OK_STATUS_CODE = 200;
    private static final int NOT_FOUND_STATUS_CODE = 404;

    private Javalin app;

    @BeforeEach
    void setUp() {
        app = App.getApp();
    }

    @AfterEach
    void tearDown() {
        app.stop();
    }

    @Test
    @DisplayName("Should handle GET / correctly")
    void checkGetRootEndpoint() {
        JavalinTest.test(app, (server, client) -> {

            var response = client.get("/");
            assertThat(response.code()).isEqualTo(OK_STATUS_CODE);

            var body = response.body();
            var bodyToString = nonNull(body) ? body.string() : "";
            assertThat(bodyToString).contains("Анализатор страниц");
        });
    }

    @Test
    @DisplayName("Should handle POST /urls with valid url correctly")
    void checkPostValidUrl() {
        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=https://some-domain.org";
            var response = client.post("/urls", requestBody);
            assertThat(response.code()).isEqualTo(OK_STATUS_CODE);

            var body = response.body();
            var bodyToString = nonNull(body) ? body.string() : "";
            assertThat(bodyToString).contains("https://some-domain.org");
        });
    }

    @Test
    @DisplayName("Should handle POST /urls with invalid url correctly")
    void checkPostInvalidUrl() {
        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=invalid";
            var response = client.post("/urls", requestBody);
            assertThat(response.code()).isEqualTo(OK_STATUS_CODE);

            var body = response.body();
            var bodyToString = nonNull(body) ? body.string() : "";
            assertThat(bodyToString).doesNotContain("invalid");
        });
    }

    @Test
    @DisplayName("Should return 404 if url not exists")
    void checkNotFound() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/urls/11111");
            assertThat(response.code()).isEqualTo(NOT_FOUND_STATUS_CODE);
        });
    }
}
