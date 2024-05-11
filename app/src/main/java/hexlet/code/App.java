package hexlet.code;

import hexlet.code.controllers.RootController;
import io.javalin.Javalin;

public final class App {
    private static final String DEFAULT_PORT = "7070";
    private App() {
    }

    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(getPort());
    }

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", DEFAULT_PORT);
        return Integer.parseInt(port);
    }

    private static String getMode() {
        return System.getenv().getOrDefault("APP_ENV", "development");
    }

    private static boolean isProduction() {
        return getMode().equals("production");
    }

    private static void addRoutes(Javalin app) {
        app.get("/", RootController.welcome);
    }

    public static Javalin getApp() {
        Javalin app = Javalin.create(config -> {
            if (!isProduction()) {
                config.bundledPlugins.enableDevLogging();
            }
        });
        addRoutes(app);
        app.before(ctx -> ctx.attribute("ctx", ctx));
        return app;
    }
}
