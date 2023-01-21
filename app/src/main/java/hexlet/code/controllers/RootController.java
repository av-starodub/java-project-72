package hexlet.code.controllers;

import io.javalin.http.Handler;

public final class RootController {
    private RootController() {
    }

    public static Handler welcome = ctx -> ctx.result("Hello World!");
}
