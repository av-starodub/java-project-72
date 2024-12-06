package hexlet.code.controllers;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.page.UrlDto;
import hexlet.code.page.UrlPage;
import hexlet.code.page.UrlsPage;
import hexlet.code.repository.UrlCheckDao;
import hexlet.code.repository.UrlDao;
import hexlet.code.utils.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import io.javalin.rendering.template.TemplateUtil;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

import java.net.URI;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.nonNull;

@Slf4j
public final class UrlController {

    private final UrlDao urlDao;

    private final UrlCheckDao urlCheckDao;

    public UrlController(UrlDao uDao, UrlCheckDao checkDao) {
        urlDao = uDao;
        urlCheckDao = checkDao;
    }

    public Handler create() {
        return ctx -> {
            var inputUrl = ctx.formParam("url");
            requireNonNull(inputUrl, "Parameter inputUrl must not be null");

            var parsedUrl = new URI("");
            try {
                parsedUrl = new URI(inputUrl.toLowerCase().strip());
            } catch (Exception e) {
                ctx.sessionAttribute("flash", "Некорректный URL");
                ctx.sessionAttribute("alertType", "danger");
                ctx.redirect(NamedRoutes.root());
            }

            var normalizedUrl = "%s://%s%s".formatted(
                    parsedUrl.getScheme(),
                    parsedUrl.getHost(),
                    parsedUrl.getPort() == -1 ? "" : ":" + parsedUrl.getPort()
            );

            var isUrlExist = urlDao.findByName(normalizedUrl).isPresent();

            if (isUrlExist) {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("alertType", "info");
            } else {
                var newUrl = new Url(normalizedUrl);
                urlDao.save(newUrl);
                ctx.sessionAttribute("flash", "Страница успешно добавлена");
                ctx.sessionAttribute("alertType", "success");
            }
            ctx.redirect(NamedRoutes.urlsAll());
        };
    }

    public Handler showAll() {
        return ctx -> {
            var urls = urlDao.findAll();
            var latestChecks = urlCheckDao.findLatestChecks();
            var urlDtos = urls.stream()
                    .map(url -> {
                        var latestCheck = latestChecks.getOrDefault(url.getId(), new UrlCheck());
                        return UrlDto.toUrlDto(url, latestCheck);
                    })
                    .toList();
            var page = new UrlsPage(urlDtos);
            page.setFlash(ctx.consumeSessionAttribute("flash"));
            page.setAlertType(ctx.consumeSessionAttribute("alertType"));
            ctx.render("urls.jte", TemplateUtil.model("page", page));
        };
    }

    public Handler showById() {
        return ctx -> {
            var id = ctx.pathParamAsClass("id", Long.class).get();
            var url = urlDao.findById(id).orElseThrow(() -> new NotFoundResponse("Page not found"));
            var checks = urlCheckDao.findChecksByUrlId(id);
            var page = new UrlPage(url, checks);
            page.setFlash(ctx.consumeSessionAttribute("flash"));
            page.setAlertType(ctx.consumeSessionAttribute("alertType"));
            ctx.render("url.jte", TemplateUtil.model("page", page));
        };
    }

    public Handler checkUrl() {
        return ctx -> {
            var urlId = ctx.pathParamAsClass("id", Long.class).get();
            try {
                var url = urlDao.findById(urlId).orElseThrow(() -> new NotFoundResponse(
                        "Page with id=%s not found in data base".formatted(urlId))
                );
                executeCheck(ctx, url);
            } catch (NotFoundResponse e) {
                log.error(e.getMessage(), e);
                ctx.sessionAttribute("flash", e.getMessage());
                ctx.sessionAttribute("flash-type", "danger");
            } catch (Exception e) {
                log.error("Server error: {}", e.getMessage(), e);
                ctx.sessionAttribute("flash", e.getMessage());
                ctx.sessionAttribute("alertType", "danger");
            } finally {
                Unirest.shutDown();
                ctx.redirect(NamedRoutes.urlById(urlId));
            }
        };
    }

    private void executeCheck(Context ctx, Url url) {
        try {
            Unirest.get(url.getName())
                    .asString()
                    .ifSuccess(response -> {
                        var body = Jsoup.parse(response.getBody());
                        var desc = body.selectFirst("meta[name=description]");
                        var newCheck = UrlCheck.builder()
                                .urlId(url.getId())
                                .statusCode(response.getStatus())
                                .title(body.title())
                                .h1(body.selectFirst("h1").text())
                                .description(nonNull(desc) ? desc.attr("content") : "")
                                .build();
                        urlCheckDao.save(newCheck);
                        ctx.sessionAttribute("flash", "Страница успешно проверена");
                        ctx.sessionAttribute("alertType", "success");
                    })
                    .ifFailure(response -> {
                        log.error("Request failed with status: {}, body: {}", response.getStatus(), response.getBody());
                        response.getParsingError().ifPresent(error ->
                                log.error("Parsing error: {}", error.getMessage(), error));
                        ctx.status(response.getStatus());
                        ctx.sessionAttribute("flash", "Ошибка проверки");
                        ctx.sessionAttribute("alertType", "danger");
                    });
        } catch (UnirestException e) {
            log.error("UnirestException occurred: {}", e.getMessage(), e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.sessionAttribute("flash", "Ошибка подключения к серверу");
            ctx.sessionAttribute("alertType", "danger");
        }
    }
}
