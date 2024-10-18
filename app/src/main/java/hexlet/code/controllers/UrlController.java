package hexlet.code.controllers;

import hexlet.code.model.Url;
import hexlet.code.page.UrlPage;
import hexlet.code.page.UrlsPage;
import hexlet.code.repository.UrlDao;
import hexlet.code.repository.exception.DuplicateUrlException;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import io.javalin.rendering.template.TemplateUtil;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static java.util.Objects.isNull;

public final class UrlController {
    private final UrlDao urlDao;

    public UrlController(UrlDao dao) {
        this.urlDao = dao;
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
                ctx.redirect("/urls");
            } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
                ctx.sessionAttribute("flash", "Некорректный URL");
                ctx.sessionAttribute("alertType", "danger");
                ctx.redirect("/");
            } catch (DuplicateUrlException e) {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("alertType", "danger");
                ctx.redirect("/urls");
            }
        };
    }

    public Handler showAll() {
        return ctx -> {
            var urls = urlDao.findAll();
            var page = new UrlsPage(urls);
            page.setFlash(ctx.consumeSessionAttribute("flash"));
            page.setAlertType(ctx.consumeSessionAttribute("alertType"));
            ctx.render("urls.jte", TemplateUtil.model("page", page));
        };
    }

    public Handler showById() {
        return ctx -> {
            var id = ctx.pathParamAsClass("id", Long.class).get();
            var url = urlDao.findById(id).orElseThrow(() -> new NotFoundResponse("Страница не найдена"));
            var page = new UrlPage(url);
            page.setFlash(ctx.consumeSessionAttribute("flash"));
            page.setAlertType(ctx.consumeSessionAttribute("alertType"));
            ctx.render("url.jte", TemplateUtil.model("page", page));
        };
    }
}
