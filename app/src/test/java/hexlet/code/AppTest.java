package hexlet.code;

import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class AppTest {
    private static final int OK_STATUS_CODE = 200;

    private Javalin app;

    @BeforeEach
    void getUp() {
        app = App.getApp();
    }

    @AfterEach
    void tearDown() {
        app.stop();
    }

    @Test
    @DisplayName("checkGetRootEndpoint - should handle GET / correctly")
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
    @DisplayName("checkGetRootEndpoint - should handle POST /urls correctly")
    void checkPostToUrlsEndpoint() {
        JavalinTest.test(app, (server, client) -> {
            var requestBody = "name=https://some-domain.org";
            var response = client.post("/urls");
            assertThat(response.code()).isEqualTo(OK_STATUS_CODE);

            var body = response.body();
            var bodyToString = nonNull(body) ? body.string() : "";
            assertThat(bodyToString).contains("https://some-domain.org");
        });
    }

}
