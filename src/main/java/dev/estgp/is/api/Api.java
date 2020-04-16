package dev.estgp.is.api;

import com.google.gson.Gson;
import dev.estgp.is.Application;
import dev.estgp.is.models.Customer;
import dev.estgp.is.models.Invoice;
import dev.estgp.is.models.User;

import static spark.Spark.*;

public class Api {
    public static void defineRoutes() {
        // Set up Gson
        Gson gson = new Gson();

        // API endpoints
        path("/api", () -> {

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

            // Intercept all api requests to verify authentication
            before("/*", (request, response) -> {
                // Only let the login pass
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

            // Returns a list of all the logged user customers
            get("/customers/", (request, response) -> {
                User user = request.attribute(Application.USER_KEY);
                return gson.toJson(Customer.get(user));
            });

            // Creates a new Customer to the logged user
            post("/customers/", (request, response) -> {
                User user = request.attribute(Application.USER_KEY);
                Customer customer = gson.fromJson(request.body(), Customer.class);
                customer.user_id = user.id;
                customer.id = customer.save();
                response.status(201);
                return gson.toJson(customer);
            });

            // Intercept all requests to customers to verify if the customer id is one of the logged user
            before("/customers/:id/*", (request, response) -> {
                User user = request.attribute(Application.USER_KEY);
                String id = request.params(":id");
                int idInt = 0;
                try {
                    idInt = Integer.parseInt(id);
                } catch (NumberFormatException e) {
                    halt(400);
                }
                Customer customer = Customer.get(idInt);
                if (customer == null) {
                    halt(404);
                }
                else if (customer.user_id != user.id) {
                    halt(403);
                }
                else {
                    request.attribute(Application.CUSTOMER_KEY, customer);
                }
            });

            // Returns a customer by id
            get("/customers/:id/", (request, response) -> {
                Customer customer = request.attribute(Application.CUSTOMER_KEY);
                return gson.toJson(customer);
            });

            // Updates a Customer
            put("/customers/:id/", (request, response) -> {
                Customer customerDb = request.attribute(Application.CUSTOMER_KEY);
                Customer customer = gson.fromJson(request.body(), Customer.class);
                if (customer.name != null && !customer.name.isBlank())
                    customerDb.name = customer.name;
                if (customer.email != null && !customer.email.isBlank())
                    customerDb.email = customer.email;
                customerDb.save();
                response.status(200);
                return gson.toJson(customerDb);
            });

            // Deletes a Customer
            delete("/customers/:id/", (request, response) -> {
                Customer customer = request.attribute(Application.CUSTOMER_KEY);
                customer.delete();
                return "";
            });

            // Returns a list of all the Invoices of the Customer
            get("/customers/:id/invoices/", (request, response) -> {
                Customer customer = request.attribute(Application.CUSTOMER_KEY);
                return gson.toJson(Invoice.getByCustomer(customer));
            });

            // Creates a new Invoice for the Customer
            post("/customers/:id/invoices/", (request, response) -> {
                Customer customer = request.attribute(Application.CUSTOMER_KEY);
                Invoice invoice = gson.fromJson(request.body(), Invoice.class);
                invoice.customer_id = customer.id;
                invoice.id = customer.save();
                response.status(201);
                return gson.toJson(invoice);
            });

            // TODO get /api/customers/:id/invoices/:invoiceId

            // Intercept all requests to Invoice to verify if the customer id is one of the logged user
            before("/invoices/:id/*", (request, response) -> {
                User user = request.attribute(Application.USER_KEY);
                String id = request.params(":id");
                int idInt = 0;
                try {
                    idInt = Integer.parseInt(id);
                } catch (NumberFormatException e) {
                    halt(400);
                }
                Invoice invoice = Invoice.get(idInt);
                if (invoice == null) {
                    halt(404);
                }
                else {
                    Customer customer = Customer.get(invoice.customer_id);
                    if (customer.user_id != user.id) {
                        halt(403);
                    }
                    else {
                        request.attribute(Application.CUSTOMER_KEY, customer);
                        request.attribute(Application.INVOICE_KEY, invoice);
                    }
                }
            });

            // Returns a Invoice by id
            get("/invoices/:id/", (request, response) -> {
                Invoice invoice = request.attribute(Application.INVOICE_KEY);
                return gson.toJson(invoice);
            });

            // Updates a Invoice
            put("/invoices/:id/", (request, response) -> {
                Invoice invoiceDb = request.attribute(Application.INVOICE_KEY);
                Invoice invoice = gson.fromJson(request.body(), Invoice.class);
                if (invoice.date != null && !invoice.date.isBlank())
                    invoiceDb.date = invoice.date;
                // TODO Check for 0?
                invoiceDb.amount = invoice.amount;
                invoiceDb.save();
                response.status(200);
                return gson.toJson(invoiceDb);
            });

            // Deletes a Invoice
            delete("/invoices/:id/", (request, response) -> {
                Invoice invoice = request.attribute(Application.INVOICE_KEY);
                invoice.delete();
                return "";
            });

            // TODO Complete Invoice
            //      Check for security breaches

        });
    }
}
