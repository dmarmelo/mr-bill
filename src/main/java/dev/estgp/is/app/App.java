package dev.estgp.is.app;

import dev.estgp.is.Application;
import dev.estgp.is.models.Costumer;
import dev.estgp.is.models.Invoice;
import dev.estgp.is.models.User;
import dev.estgp.is.utils.freemarker.FreemarkerContext;

import java.util.List;

import static spark.Spark.*;

public class App {
    public static void defineRoutes() {
        // Website endpoints
        get("/", (request, response) -> {
            return Application.engine.render(null, "index.ftl");
        });

        get("/hello/:name/", (request, response) -> {
            FreemarkerContext context = new FreemarkerContext();
            context.put("name", request.params(":name"));
            return Application.engine.render(context, "hello.ftl");
        });

        get("/login/", (request, response) -> {
            return Application.engine.render(null, "login.ftl");
        });

        post("/login/", (request, response) -> {
            String username = request.queryParams("username");
            String password = request.queryParams("password");
            User user = User.authenticate(username, password);
            if (user != null) {
                request.session().attribute("user", user);
                response.redirect("/app/");
            }
            else {
                response.redirect("/login/");
            }
            return "";
        });

        get("/logout/", (request, response) -> {
            request.session().removeAttribute("user");
            response.redirect("/login/");
            return "";
        });

        path("/app", () -> {
            before("/*", (request, response) -> {
                User user = request.session().attribute("user");
                if (user == null) {
                    response.redirect("/login/");
                    halt();
                }
            });

            get("/", (request, response) -> {
                FreemarkerContext context = new FreemarkerContext();
                User user = request.session().attribute("user");
                context.put("user", user);
                context.put("costumers", Costumer.getByUser(user));
                return Application.engine.render(context, "app.ftl");
            });

            post("/costumer/", (request, response) -> {
                String name = request.queryParams("name");
                String email = request.queryParams("email");
                User user = request.session().attribute("user");
                Costumer costumer = new Costumer();
                costumer.user_id = user.id;
                costumer.name = name;
                costumer.email = email;
                costumer.save();
                response.redirect("/app/");
                return "";
            });

            get("/costumer/:id/", (request, response) -> {
                User user = request.session().attribute("user");
                String id = request.params(":id");
                Costumer costumer = Costumer.get(Integer.parseInt(id));
                if (costumer == null) {
                    Application.sendError(404);
                }
                if (costumer.user_id != user.id) {
                    // Eventualmente um before
                    Application.sendError(403);
                }
                List<Invoice> invoices = Invoice.getByCostumer(costumer);

                FreemarkerContext context = new FreemarkerContext();
                context.put("user", user);
                context.put("costumer", costumer);
                context.put("invoices", invoices);
                return Application.engine.render(context, "costumer.ftl");
            });

            get("/costumer/:id/delete/", (request, response) -> {
                User user = request.session().attribute("user");
                String id = request.params(":id");
                Costumer costumer = Costumer.get(Integer.parseInt(id));
                if (costumer == null) {
                    Application.sendError(404);
                }
                if (costumer.user_id != user.id) {
                    // Eventualmente um before
                    Application.sendError(403);
                }
                // Buscar vários invoices a remover
                costumer.delete();
                response.redirect("/app/");
                return "";
            });

            // TODO website
            // 1. /app/costumer/:id/ - Validar que o user é "dono" destes costumers. Usar regra "before"
            // 2. Permitir actualizar e remover Costumers e Invoices
        });

    }
}
