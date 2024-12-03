package hexlet.code;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.code.controllers.RootController;
import hexlet.code.controllers.UrlController;
import hexlet.code.repository.AbstractBaseDao;
import hexlet.code.repository.UrlCheckDao;
import hexlet.code.repository.UrlDao;
import hexlet.code.utils.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;

@Slf4j
public final class App {
    public static final String DEFAULT_PORT = "7071";

    private App() {
    }

    public static void main(String[] args) {
        flywayMigrate();
        var app = getApp();
        var port = getPort();
        app.start(port);
    }

    public static Javalin getApp() {
        Javalin app = Javalin.create(config -> {
            if (!"production".equals(System.getenv().get("APP_MODE"))) {
                config.bundledPlugins.enableDevLogging();
                log.info("Javalin developer mode logging enabled ");
            }
            config.fileRenderer(new JavalinJte(createTemplateEngine()));
        });
        addRoutes(app);
        app.before(ctx -> ctx.attribute("ctx", ctx));
        return app;
    }

    private static TemplateEngine createTemplateEngine() {
        var classLoader = App.class.getClassLoader();
        var codeResolver = new ResourceCodeResolver("templates", classLoader);
        return TemplateEngine.create(codeResolver, ContentType.Html);
    }

    private static void addRoutes(Javalin app) {
        var urlDao = new UrlDao();
        var urlCheckDao = new UrlCheckDao();
        var urlController = new UrlController(urlDao, urlCheckDao);
        app.get(NamedRoutes.root(), RootController.root());
        app.post(NamedRoutes.urlNew(), urlController.create());
        app.get(NamedRoutes.urlsAll(), urlController.showAll());
        app.get(NamedRoutes.urlById(), urlController.showById());
        app.post(NamedRoutes.checkNew(), urlController.checkUrl());
    }

    private static int getPort() {
        var port = System.getenv().getOrDefault("PORT", DEFAULT_PORT);
        return Integer.parseInt(port);
    }

    public static void flywayMigrate() {
        log.info("db migration started...");
        var flyway = Flyway.configure()
                .dataSource(AbstractBaseDao.DATASOURCE)
                .locations("classpath:/db/migration")
                .load();
        flyway.migrate();
        log.info("db migration finished.");
        log.info("***");
    }
}
