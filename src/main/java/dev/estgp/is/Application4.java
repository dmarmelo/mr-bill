package dev.estgp.is;

import com.google.gson.Gson;
import dev.estgp.is.models.Customer;
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
            context.put("customers", Customer.get(user));
            return engine.render(context, "app.ftl");
        });

        post("/app/customer/", (request, response) -> {
            User user = request.session().attribute("user");
            String name = request.queryParams("name");
            String email = request.queryParams("email");
            Customer customer = new Customer();
            customer.name = name;
            customer.email = email;
            customer.user_id = user.id;
            customer.save();
            response.redirect("/app/");
            return "";
        });

        get("/app/customer/:id/", (request, response) -> {
            String id = request.params(":id");
            FreemarkerContext context = new FreemarkerContext();
            User user = request.session().attribute("user");
            context.put("user", user);
            Customer customer = Customer.get(Integer.parseInt(id));
            context.put("customer", customer);
            context.put("invoices", Invoice.get(customer));
            return engine.render(context, "customer.ftl");
        });

        get("/app/customer/:id/delete/", (request, response) -> {
            String id = request.params(":id");
            Customer customer = Customer.get(Integer.parseInt(id));
            customer.delete();
            response.redirect("/app/");
            return "";
        });

        post("/app/customer/:id/invoice/", (request, response) -> {
            String id = request.params(":id");
            User user = request.session().attribute("user");
            Customer customer = Customer.get(Integer.parseInt(id));
            Invoice invoice = new Invoice();
            invoice.customer_id = customer.id;
            invoice.date = request.queryParams("date");
            invoice.amount = Float.parseFloat(request.queryParams("amount"));
            invoice.save();
            response.redirect("/app/customer/" + id + "/");
            return "";
        });

        // TODO website
        // 1. /app/customer/:id/ - Validar que o user é "dono" destes customers. Usar regra "before"
        // 2. Permitir actualizar e remover Customers e Invoices

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

        get("/api/customers/", (request, response) -> {
            // Retornar a lista de todos os meus clientes
            User user = request.attribute(USER_KEY);
            return gson.toJson(Customer.get(user));
        });

        get("/api/customers/:id/", (request, response) -> {
            User user = request.attribute(USER_KEY);
            String customerId = request.params(":id");
            Customer customer = Customer.get(Integer.parseInt(customerId));
            if (customer != null) {
                if (customer.user_id == user.id) {
                    return gson.toJson(customer);
                } else
                    response.status(403);
            } else
                response.status(404);
            return "";
        });

        post("/api/customers/", (request, response) -> {
            User user = request.attribute(USER_KEY);
            Customer customer = gson.fromJson(request.body(), Customer.class);
            customer.user_id = user.id;
            customer.id = customer.save();
            response.status(201);
            return gson.toJson(customer);
        });

        delete("/api/customers/:id/", (request, response) -> {
            User user = request.attribute(USER_KEY);
            String customerId = request.params(":id");
            Customer customer = Customer.get(Integer.parseInt(customerId));
            if (customer != null) {
                if (customer.user_id == user.id) {
                    customer.delete();
                } else {
                    halt(403);
                }
            } else {
                halt(404);
            }
            return "";
        });

        // get /api/customers/:id/invoices
        // get /api/customers/:id/invoices/:invoiceId
        // before's segurança

    }
}
