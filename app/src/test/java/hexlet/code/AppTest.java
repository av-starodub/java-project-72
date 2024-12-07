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
import java.util.regex.Pattern;

import static kong.unirest.HttpStatus.OK;
import static kong.unirest.HttpStatus.NOT_FOUND;
import static kong.unirest.HttpStatus.INTERNAL_SERVER_ERROR;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class AppTest {

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
            assertThat(response.code()).isEqualTo(OK);

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
            assertThat(response.code()).isEqualTo(OK);

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
            assertThat(response.code()).isEqualTo(INTERNAL_SERVER_ERROR);
        });
    }

    @Test
    @DisplayName("Should handle POST /urls with invalid url correctly")
    void checkPostInvalidUrl() {
        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=invalid";
            var response = client.post(NamedRoutes.urlNew(), requestBody);
            assertThat(response.code()).isEqualTo(OK);

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
            assertThat(response.code()).isEqualTo(NOT_FOUND);
        });
    }

    @Test
    @DisplayName("Should handle adding a duplicate Url correctly")
    void checkSaveDuplicateUrl() {
        JavalinTest.test(app, ((server, client) -> {
            var requestBody = "url=https://some-domain.org";
            var postUrlResponse = client.post(NamedRoutes.urlNew(), requestBody);
            assertThat(postUrlResponse.code()).isEqualTo(OK);

            var postDuplicateUrlResponse = client.post(NamedRoutes.urlNew(), requestBody);
            assertThat(postDuplicateUrlResponse.code()).isEqualTo(OK);

            var body = postDuplicateUrlResponse.body();
            assertThat(body).isNotNull();

            var pattern = Pattern.compile(Pattern.quote("https://some-domain.org"));
            var matcher = pattern.matcher(body.string());
            int savedSameUrlCount = 0;
            while (matcher.find()) {
                savedSameUrlCount++;
            }
            assertThat(savedSameUrlCount).isEqualTo(1);
        }));
    }
    @Test
    @DisplayName("Should handle POST /urls/{id}/checks with success check correctly")
    void checkSuccessUrlCheck() throws IOException {
        var mockWebServer = new MockWebServer();
        mockWebServer.start();
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(OK)
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
            var postUrlResponse = client.post(NamedRoutes.urlNew(), requestBody);
            assertThat(postUrlResponse.code()).isEqualTo(OK);

            var postUrlCheckResponse = client.post(NamedRoutes.checkNew(1L));
            assertThat(postUrlCheckResponse.code()).isEqualTo(OK);

            var uroCheckResponseBody = postUrlCheckResponse.body();
            assertThat(uroCheckResponseBody).isNotNull();
            assertThat(uroCheckResponseBody.string())
                    .contains("Test Page")
                    .contains("Test Description")
                    .contains("Test H1");

            var getSavedUrlCheckResponse = client.get(NamedRoutes.urlById(1L));
            assertThat(getSavedUrlCheckResponse.code()).isEqualTo(OK);

            var savedUrlCheckResponseBody = getSavedUrlCheckResponse.body();
            assertThat(savedUrlCheckResponseBody).isNotNull();
            assertThat(savedUrlCheckResponseBody.string())
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
            assertThat(checkResponse.code()).isEqualTo(NOT_FOUND);
        });
    }

    @Test
    @DisplayName("Should handle POST /urls/{id}/checks with DataBaseOperationException correctly")
    void checkUrlCheckWithDataBaseException() {
        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=https://some-domain.org";
            var response = client.post(NamedRoutes.urlNew(), requestBody);
            assertThat(response.code()).isEqualTo(OK);

            App.FLYWAY.clean();

            var checkResponse = client.post(NamedRoutes.checkNew(1L));
            assertThat(checkResponse.code()).isEqualTo(INTERNAL_SERVER_ERROR);
        });
    }
}
