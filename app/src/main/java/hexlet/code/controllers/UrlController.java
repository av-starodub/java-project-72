package hexlet.code.controllers;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.page.UrlPage;
import hexlet.code.page.UrlsPage;
import hexlet.code.repository.UrlCheckDao;
import hexlet.code.repository.UrlDao;
import hexlet.code.repository.exception.DuplicateUrlException;
import hexlet.code.utils.NamedRoutes;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import io.javalin.rendering.template.TemplateUtil;
import kong.unirest.core.Unirest;
import org.jsoup.Jsoup;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public final class UrlController {

    private final UrlDao urlDao;

    private final UrlCheckDao urlCheckDao;

    public UrlController(UrlDao uDao, UrlCheckDao checkDao) {
        urlDao = uDao;
        urlCheckDao = checkDao;
    }

    public Handler create() {
        return ctx -> {
            var userInput = ctx.formParam("url");
            try {
                if (isNull(userInput)) {
                    throw new IllegalArgumentException();
                }
                var uri = new URI(userInput.toLowerCase().strip());
                var url = uri.toURL();
                var protocol = url.getProtocol();
                var host = url.getHost();
                var port = url.getPort();
                var normalizedUrl = "%s://%s%s".formatted(protocol, host, port != -1 ? ":" + port : "");
                var urlEntity = new Url();
                urlEntity.setName(normalizedUrl);
                urlDao.save(urlEntity);
                ctx.sessionAttribute("flash", "Страница успешно добавлена");
                ctx.sessionAttribute("alertType", "success");
                ctx.redirect(NamedRoutes.urlsAll());
            } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
                ctx.sessionAttribute("flash", "Некорректный URL");
                ctx.sessionAttribute("alertType", "danger");
                ctx.redirect(NamedRoutes.root());
            } catch (DuplicateUrlException e) {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("alertType", "danger");
                ctx.redirect(NamedRoutes.urlsAll());
            }
        };
    }

    public Handler showAll() {
        return ctx -> {
            var urls = urlDao.findAll();
            var urlToLastCheckMap = urlDao.findAll().stream()
                    .collect(Collectors.toMap(
                            url -> url,
                            url -> urlCheckDao.findLastCheckByUrlId(url.getId()).orElseGet(UrlCheck::new))
                    );
            var page = new UrlsPage(urlToLastCheckMap);
            page.setFlash(ctx.consumeSessionAttribute("flash"));
            page.setAlertType(ctx.consumeSessionAttribute("alertType"));
            ctx.render("urls.jte", TemplateUtil.model("page", page));
        };
    }

    public Handler showById() {
        return ctx -> {
            var id = ctx.pathParamAsClass("id", Long.class).get();
            var url = urlDao.findById(id).orElseThrow(() -> new NotFoundResponse("Страница не найдена"));
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
            var url = urlDao.findById(urlId).orElseThrow(() -> new NotFoundResponse("Страница не найдена"));
            Unirest.get(url.getName())
                    .asString()
                    .ifSuccess(response -> {
                        var body = Jsoup.parse(response.getBody());
                        var desc = body.selectFirst("meta[name=description]");
                        var newCheck = UrlCheck.builder()
                                .urlId(urlId)
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
                        ctx.status(response.getStatus());
                        ctx.sessionAttribute("flash", "Ошибка проверки");
                        ctx.sessionAttribute("alertType", "danger");
                    });
            ctx.redirect(NamedRoutes.urlById(urlId));
            Unirest.shutDown();
        };
    }
}
