package hexlet.code;

import hexlet.code.utils.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class AppTest {

    private static final int OK_STATUS_CODE = 200;

    private static final int NOT_FOUND_STATUS_CODE = 404;

    private static final int SERVER_ERROR_STATUS_CODE = 500;

    private Javalin app;

    @BeforeEach
    void setUp() {
        app = App.getApp();
    }

    @AfterEach
    void tearDown() {
        App.FLYWAY.clean();
    }

    @Test
    @DisplayName("Should handle GET / correctly")
    void checkGetRootEndpoint() {
        JavalinTest.test(app, (server, client) -> {

            var response = client.get(NamedRoutes.root());
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
            var response = client.post(NamedRoutes.urlNew(), requestBody);
            assertThat(response.code()).isEqualTo(OK_STATUS_CODE);

            var body = response.body();
            var bodyToString = nonNull(body) ? body.string() : "";
            assertThat(bodyToString).contains("https://some-domain.org");
        });
    }

    @Test
    @DisplayName("Should handle POST /urls with DataBaseOperationException correctly")
    void checkPostValidUrlWithDataBaseException() {
        JavalinTest.test(app, (server, client) -> {

            App.FLYWAY.clean();

            var requestBody = "url=https://some-domain.org";
            var response = client.post(NamedRoutes.urlNew(), requestBody);
            assertThat(response.code()).isEqualTo(SERVER_ERROR_STATUS_CODE);
        });
    }

    @Test
    @DisplayName("Should handle POST /urls with invalid url correctly")
    void checkPostInvalidUrl() {
        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=invalid";
            var response = client.post(NamedRoutes.urlNew(), requestBody);
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
            var response = client.get(NamedRoutes.urlById(0L));
            assertThat(response.code()).isEqualTo(NOT_FOUND_STATUS_CODE);
        });
    }

    @Test
    @DisplayName("Should handle POST /urls/{id}/checks with success check correctly")
    void checkSuccessUrlCheck() throws IOException {
        var mockWebServer = new MockWebServer();
        mockWebServer.start();
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(OK_STATUS_CODE)
                .setBody("""
                        <html>
                            <head>
                                <title>Test Page</title>
                                <meta name="description" content="Test Description">
                            </head>
                            <body>
                                <h1>Test H1</h1>
                            </body>
                        </html>
                        """));
        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=" + mockWebServer.url("/");
            var postResponse = client.post(NamedRoutes.urlNew(), requestBody);
            assertThat(postResponse.code()).isEqualTo(OK_STATUS_CODE);

            var checkResponse = client.post(NamedRoutes.checkNew(1L));
            assertThat(checkResponse.code()).isEqualTo(OK_STATUS_CODE);

            var body = checkResponse.body();
            assertThat(body).isNotNull();
            assertThat(body.string())
                    .contains("Test Page")
                    .contains("Test Description")
                    .contains("Test H1");
        });
        mockWebServer.shutdown();
        mockWebServer.close();
    }

    @Test
    @DisplayName("Should handle POST /urls/{id}/checks when url not found correctly")
    void checkNotFoundUrlCheck() {
        JavalinTest.test(app, (server, client) -> {
            var checkResponse = client.post(NamedRoutes.checkNew(1L));
            assertThat(checkResponse.code()).isEqualTo(NOT_FOUND_STATUS_CODE);
        });
    }

    @Test
    @DisplayName("Should handle POST /urls/{id}/checks with DataBaseOperationException correctly")
    void checkUrlCheckWithDataBaseException() {
        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=https://some-domain.org";
            var response = client.post(NamedRoutes.urlNew(), requestBody);
            assertThat(response.code()).isEqualTo(OK_STATUS_CODE);

            App.FLYWAY.clean();

            var checkResponse = client.post(NamedRoutes.checkNew(1L));
            assertThat(checkResponse.code()).isEqualTo(SERVER_ERROR_STATUS_CODE);
        });
    }
}
