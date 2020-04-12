package dev.estgp.is.api;

import com.google.gson.Gson;
import dev.estgp.is.Application;
import dev.estgp.is.models.Costumer;
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

            get("/costumers/", (request, response) -> {
                // Retornar a lista de todos os meus clientes
                User user = request.attribute(Application.USER_KEY);
                return gson.toJson(Costumer.get(user));
            });

            get("/costumers/:id/", (request, response) -> {
                User user = request.attribute(Application.USER_KEY);
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

            post("/costumers/", (request, response) -> {
                User user = request.attribute(Application.USER_KEY);
                Costumer costumer = gson.fromJson(request.body(), Costumer.class);
                costumer.user_id = user.id;
                costumer.id = costumer.save();
                response.status(201);
                return gson.toJson(costumer);
            });

            delete("/costumers/:id/", (request, response) -> {
                User user = request.attribute(Application.USER_KEY);
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

        });

    }
}
