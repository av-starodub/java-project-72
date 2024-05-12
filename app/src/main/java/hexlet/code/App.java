package hexlet.code;

import hexlet.code.controllers.RootController;
import hexlet.code.repository.AbstractDao;
import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;

@Slf4j
public final class App {
    private static final String DEFAULT_PORT = "7070";

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
                log.info("enable dev logging ");
            }
        });
        addRoutes(app);
        app.before(ctx -> ctx.attribute("ctx", ctx));
        return app;
    }

    private static void addRoutes(Javalin app) {
        app.get("/", RootController.welcome);
    }

    private static int getPort() {
        var port = System.getenv().getOrDefault("PORT", DEFAULT_PORT);
        return Integer.parseInt(port);
    }

    private static void flywayMigrate() {
        log.info("db migration started...");
        var flyway = Flyway.configure()
                .dataSource(AbstractDao.DATASOURCE)
                .locations("classpath:/db/migration")
                .load();
        flyway.migrate();
        log.info("db migration finished.");
        log.info("***");
    }
}
