package hexlet.code.controllers;

import hexlet.code.page.BasePage;
import io.javalin.http.Handler;
import io.javalin.rendering.template.TemplateUtil;

public final class RootController {

    private RootController() {
    }

    public static Handler root() {
        return ctx -> {
            var page = new BasePage();
            page.setFlash(ctx.consumeSessionAttribute("flash"));
            page.setAlertType(ctx.consumeSessionAttribute("alertType"));
            ctx.render("main.jte", TemplateUtil.model("page", page));
        };
    }
}
