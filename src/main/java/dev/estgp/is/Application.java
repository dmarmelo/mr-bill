package dev.estgp.is;

import com.google.gson.Gson;
import dev.estgp.is.models.Costumer;
import dev.estgp.is.models.Invoice;
import dev.estgp.is.models.User;
import dev.estgp.is.utils.freemarker.FreemarkerContext;
import dev.estgp.is.utils.freemarker.FreemarkerEngine;
import dev.estgp.is.utils.sqlite3.SQLiteConn;

import java.util.List;

import static spark.Spark.*;

/**
 * Simple Spark web application
 */
public class Application {

    // Configure freemarker engine
    public static FreemarkerEngine engine = null;
    public static final String USER_KEY = "user.key";

    public static void main(String[] args) {
        // Check arguments for development mode flag
        boolean devMode = false;
        if (args.length > 0) {
            if (args[0].equals("devMode")) {
                devMode = true;
            }
        }

        // Configure Spark Port
        port(8000);

        // If in development mode set the static files directory and the templates directory to the relative path of the files
        if (devMode) {
            staticFiles.externalLocation("src/main/resources/public");
            engine = new FreemarkerEngine("src/main/resources/templates");
        }
        else { // If not in development mode set the files from the classpath
            staticFiles.location("/public");
            engine = new FreemarkerEngine(Application.class, "/templates");
        }

        // Configure database connection
        SQLiteConn conn = SQLiteConn.getSharedInstance();
        conn.init("data/database.db");
        //conn.recreate("data/database.sql");

        // Set up Gson
        Gson gson = new Gson();

        // Website endpoints
        get("/", (request, response) -> {
            return engine.render(null, "index.ftl");
        });

        get("/hello/:name/", (request, response) -> {
            FreemarkerContext context = new FreemarkerContext();
            context.put("name", request.params(":name"));
            return engine.render(context, "hello.ftl");
        });

        get("/login/", (request, response) ->
                engine.render(null, "login.ftl")
        );

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

        before("/app/*", (request, response) -> {
            User user = request.session().attribute("user");
            if (user == null) {
                response.redirect("/login/");
                halt();
            }
        });

        get("/app/", (request, response) -> {
            FreemarkerContext context = new FreemarkerContext();
            User user = request.session().attribute("user");
            context.put("user", user);
            context.put("costumers", Costumer.getByUser(user));
            return engine.render(context, "app.ftl");
        });

        post("/app/costumer/", (request, response) -> {
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

        get("/app/costumer/:id/", (request, response) -> {
            User user = request.session().attribute("user");
            String id = request.params(":id");
            Costumer costumer = Costumer.get(Integer.parseInt(id));
            if (costumer == null) {
                sendError(404);
            }
            if (costumer.user_id != user.id) {
                // Eventualmente um before
                sendError(403);
            }
            List<Invoice> invoices = Invoice.getByCostumer(costumer);

            FreemarkerContext context = new FreemarkerContext();
            context.put("user", user);
            context.put("costumer", costumer);
            context.put("invoices", invoices);
            return engine.render(context, "costumer.ftl");
        });

        get("/app/costumer/:id/delete/", (request, response) -> {
            User user = request.session().attribute("user");
            String id = request.params(":id");
            Costumer costumer = Costumer.get(Integer.parseInt(id));
            if (costumer == null) {
                sendError(404);
            }
            if (costumer.user_id != user.id) {
                // Eventualmente um before
                sendError(403);
            }
            // Buscar vários invoices a remover
            costumer.delete();
            response.redirect("/app/");
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


        get("/error/:code", (request, response) -> {
            String code = request.params(":code");
            sendError(Integer.parseInt(code));
            return "";
        });

        notFound((req, res) -> {
            FreemarkerContext context = new FreemarkerContext();
            context.put("code", 404);
            return engine.render(context, "error.ftl");
        });

        internalServerError((req, res) -> {
            FreemarkerContext context = new FreemarkerContext();
            context.put("code", 500);
            return engine.render(context, "error.ftl");
        });

    }

    public static void sendError(int code) {
        FreemarkerContext context = new FreemarkerContext();
        context.put("code", code);
        halt(code, engine.render(context, "error.ftl"));
    }
}
