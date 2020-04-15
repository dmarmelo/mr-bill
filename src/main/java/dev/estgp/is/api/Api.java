package dev.estgp.is.api;

import com.google.gson.Gson;
import dev.estgp.is.Application;
import dev.estgp.is.models.Customer;
import dev.estgp.is.models.User;

import static spark.Spark.*;

public class Api {
    public static void defineRoutes() {
        // Set up Gson
        Gson gson = new Gson();

        // API endpoints
        path("/api", () -> {
            //before("/*", (q, a) -> log.info("Received api call"));

            get("/users/", (request, response) -> {
                response.type("application/json");
                return gson.toJson(User.all());
            });

            get("/users/:id/", (request, response) -> {
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


            before("/*", (request, response) -> {
                if (request.pathInfo().startsWith("/api/user")) {
                    return;
                }

                // Try to get user
                User user = request.session().attribute(Application.USER_KEY);
                if (user == null) {
                    String apiKey = request.queryParams("apiKey");
                    user = User.get(apiKey);
                }

                // Validate user
                if (user != null) {
                    response.type("application/json");
                    request.attribute(Application.USER_KEY, user);
                } else {
                    halt(401);
                }
            });

            post("/user/login/", (request, response) -> {
                response.type("application/json");
                User user = gson.fromJson(request.body(), User.class);
                User auth = User.authenticate(user.username, user.password);
                if (auth != null) {
                    request.session().attribute(Application.USER_KEY, auth);
                } else {
                    halt(401);
                }
                return "";
            });

            get("/user/logout/", (request, response) -> {
                response.type("application/json");
                request.session().removeAttribute(Application.USER_KEY);
                return "";
            });

            get("/customers/", (request, response) -> {
                // Retornar a lista de todos os meus clientes
                User user = request.attribute(Application.USER_KEY);
                return gson.toJson(Customer.get(user));
            });

            get("/customers/:id/", (request, response) -> {
                User user = request.attribute(Application.USER_KEY);
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

            post("/customers/", (request, response) -> {
                User user = request.attribute(Application.USER_KEY);
                Customer customer = gson.fromJson(request.body(), Customer.class);
                customer.user_id = user.id;
                customer.id = customer.save();
                response.status(201);
                return gson.toJson(customer);
            });

            delete("/customers/:id/", (request, response) -> {
                User user = request.attribute(Application.USER_KEY);
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

        });

    }
}
