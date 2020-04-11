package dev.estgp.is;

import com.google.gson.Gson;
import dev.estgp.is.models.Costumer;
import dev.estgp.is.models.Invoice;
import dev.estgp.is.models.User;
import dev.estgp.is.utils.freemarker.FreemarkerContext;
import dev.estgp.is.utils.freemarker.FreemarkerEngine;
import dev.estgp.is.utils.sqlite3.SQLiteConn;

import static spark.Spark.*;

/**
 * Simple Spark web application
 */
public class Application4 {

    public static final String USER_KEY = "user.key";

    public static void main(String[] args) {

        // Configure Spark
        port(8000);
        staticFiles.externalLocation("resources");

        // Configure freemarker engine
        FreemarkerEngine engine = new FreemarkerEngine("resources/templates");

        // Configure database connection
        SQLiteConn conn = SQLiteConn.getSharedInstance();
        conn.init("data/database.db");
        conn.recreate("data/database.sql");

        // Set up Gson
        Gson gson = new Gson();

        // Website endpoints
        get("/", (request, response) -> {
            return engine.render(null, "index.ftl");
        });

        get("/login/", (request, response) -> {
            return engine.render(null, "login.ftl");
        });

        post("/login/", (request, response) -> {
            String username = request.queryParams("username");
            String password = request.queryParams("password");
            User user = User.authenticate(username, password);
            request.session().attribute("user", user);
            response.redirect("/app/");
            return "";
        });

        get("/logout/", (request, response) -> {
            request.session().removeAttribute("user");
            response.redirect("/login/");
            return "";
        });

        before("/app/*", (request, response) -> {
            User user = request.session().attribute("user");
            if (user == null) {
                response.redirect("/login/");
            }
        });

        get("/app/", (request, response) -> {
            FreemarkerContext context = new FreemarkerContext();
            User user = request.session().attribute("user");
            context.put("user", user);
            context.put("costumers", Costumer.get(user));
            return engine.render(context, "app.ftl");
        });

        post("/app/costumer/", (request, response) -> {
            User user = request.session().attribute("user");
            String name = request.queryParams("name");
            String email = request.queryParams("email");
            Costumer costumer = new Costumer();
            costumer.name = name;
            costumer.email = email;
            costumer.user_id = user.id;
            costumer.save();
            response.redirect("/app/");
            return "";
        });

        get("/app/costumer/:id/", (request, response) -> {
            String id = request.params(":id");
            FreemarkerContext context = new FreemarkerContext();
            User user = request.session().attribute("user");
            context.put("user", user);
            Costumer costumer = Costumer.get(Integer.parseInt(id));
            context.put("costumer", costumer);
            context.put("invoices", Invoice.get(costumer));
            return engine.render(context, "costumer.ftl");
        });

        get("/app/costumer/:id/delete/", (request, response) -> {
            String id = request.params(":id");
            Costumer costumer = Costumer.get(Integer.parseInt(id));
            costumer.delete();
            response.redirect("/app/");
            return "";
        });

        post("/app/costumer/:id/invoice/", (request, response) -> {
            String id = request.params(":id");
            User user = request.session().attribute("user");
            Costumer costumer = Costumer.get(Integer.parseInt(id));
            Invoice invoice = new Invoice();
            invoice.costumer_id = costumer.id;
            invoice.date = request.queryParams("date");
            invoice.amount = Float.parseFloat(request.queryParams("amount"));
            invoice.save();
            response.redirect("/app/costumer/" + id + "/");
            return "";
        });

        // TODO website
        // 1. /app/costumer/:id/ - Validar que o user é "dono" destes costumers. Usar regra "before"
        // 2. Permitir actualizar e remover Costumers e Invoices

        // API endpoints
        get("/api/users/", (request, response) -> {
            response.type("application/json");
            return gson.toJson(User.all());
        });

        get("/api/users/:id/", (request, response) -> {
            response.type("application/json");
            String id = request.params(":id");
            User user = User.get(Integer.parseInt(id));
            if (user != null) {
                return gson.toJson(user);
            } else {
                response.status(404);
                return "";
            }
        });


        before("/api/*", (request, response) -> {
            if (request.pathInfo().startsWith("/api/user")) {
                return;
            }

            // Try to get user
            User user = request.session().attribute(USER_KEY);
            if (user == null) {
                String apiKey = request.queryParams("apiKey");
                user = User.get(apiKey);
            }

            // Validate user
            if (user != null) {
                response.type("application/json");
                request.attribute(USER_KEY, user);
            } else {
                halt(401);
            }
        });

        post("/api/user/login/", (request, response) -> {
            response.type("application/json");
            User user = gson.fromJson(request.body(), User.class);
            User auth = User.authenticate(user.username, user.password);
            if (auth != null) {
                request.session().attribute(USER_KEY, auth);
            } else {
                halt(401);
            }
            return "";
        });

        get("/api/user/logout/", (request, response) -> {
            response.type("application/json");
            request.session().removeAttribute(USER_KEY);
            return "";
        });

        get("/api/costumers/", (request, response) -> {
            // Retornar a lista de todos os meus clientes
            User user = request.attribute(USER_KEY);
            return gson.toJson(Costumer.get(user));
        });

        get("/api/costumers/:id/", (request, response) -> {
            User user = request.attribute(USER_KEY);
            String costumerId = request.params(":id");
            Costumer costumer = Costumer.get(Integer.parseInt(costumerId));
            if (costumer != null) {
                if (costumer.user_id == user.id) {
                    return gson.toJson(costumer);
                } else
                    response.status(403);
            } else
                response.status(404);
            return "";
        });

        post("/api/costumers/", (request, response) -> {
            User user = request.attribute(USER_KEY);
            Costumer costumer = gson.fromJson(request.body(), Costumer.class);
            costumer.user_id = user.id;
            costumer.id = costumer.save();
            response.status(201);
            return gson.toJson(costumer);
        });

        delete("/api/costumers/:id/", (request, response) -> {
            User user = request.attribute(USER_KEY);
            String costumerId = request.params(":id");
            Costumer costumer = Costumer.get(Integer.parseInt(costumerId));
            if (costumer != null) {
                if (costumer.user_id == user.id) {
                    costumer.delete();
                } else {
                    halt(403);
                }
            } else {
                halt(404);
            }
            return "";
        });

        // get /api/costumers/:id/invoices
        // get /api/costumers/:id/invoices/:invoiceId
        // before's segurança

    }
}
