package dev.estgp.is.app;

import dev.estgp.is.Application;
import dev.estgp.is.models.Customer;
import dev.estgp.is.models.Invoice;
import dev.estgp.is.models.User;
import dev.estgp.is.utils.freemarker.FreemarkerContext;
import org.eclipse.jetty.http.HttpStatus;

import java.util.List;

import static spark.Spark.*;

/**
 * App  Website endpoints
 */
public class App {
    public static void defineRoutes() {
        // Index Page
        get("/", (request, response) -> {
            FreemarkerContext context = new FreemarkerContext();
            context.put("title", "App Index");
            return Application.engine.render(context, "index.ftl");
        });

        /*get("/hello/:name/", (request, response) -> {
            FreemarkerContext context = new FreemarkerContext();
            context.put("name", request.params(":name"));
            return Application.engine.render(context, "hello.ftl");
        });*/

        // Login Page
        get("/login/", (request, response) -> {
            FreemarkerContext context = new FreemarkerContext();
            context.put("title", "Login");
            return Application.engine.render(context, "login.ftl");
        });

        // Login Action
        post("/login/", (request, response) -> {
            String username = request.queryParams("username");
            String password = request.queryParams("password");
            User user = User.authenticate(username, password);
            if (user != null) {
                request.session().attribute(Application.USER_KEY, user);
                response.redirect("/app/");
            }
            else {
                response.redirect("/login/");
            }
            return "";
        });

        // Logout Action
        get("/logout/", (request, response) -> {
            request.session().removeAttribute(Application.USER_KEY);
            response.redirect("/login/");
            return "";
        });

        // Secure Application Path
        path("/app/", () -> {
            // Intercept all requests to verify if there is a user authenticated
            before("/*", (request, response) -> {
                User user = request.session().attribute(Application.USER_KEY);
                if (user == null) {
                    response.redirect("/login/");
                    Application.sendError(HttpStatus.Code.UNAUTHORIZED);
                }
                else {
                    request.attribute(Application.USER_KEY, user);
                }
            });

            // Secure Application Index Page
            get("/", (request, response) -> {
                User user = request.attribute(Application.USER_KEY);
                FreemarkerContext context = new FreemarkerContext();
                context.put("title", "App");
                context.put("user", user);
                context.put("customers", Customer.getByUser(user));
                return Application.engine.render(context, "app.ftl");
            });

            // Creates new customer
            post("/customer/", (request, response) -> {
                User user = request.attribute(Application.USER_KEY);
                String name = request.queryParams("name");
                String email = request.queryParams("email");
                Customer customer = new Customer();
                customer.user_id = user.id;
                customer.name = name;
                customer.email = email;
                customer.save();
                response.redirect("/app/");
                return "";
            });

            // Intercept all requests to customers to verify if the customer id is one of the logged user
            before("/customer/:id/*", (request, response) -> {
                User user = request.attribute(Application.USER_KEY);
                String id = request.params(":id");
                int idInt = 0;
                try {
                    idInt = Integer.parseInt(id);
                } catch (NumberFormatException e) {
                    Application.sendError(HttpStatus.Code.BAD_REQUEST);
                }
                Customer customer = Customer.get(idInt);
                if (customer == null) {
                    Application.sendError(HttpStatus.Code.NOT_FOUND);
                }
                else if (customer.user_id != user.id) {
                    Application.sendError(HttpStatus.Code.FORBIDDEN);
                }
                else {
                    request.attribute(Application.CUSTOMER_KEY, customer);
                }
            });

            // Customer page with Invoices
            get("/customer/:id/", (request, response) -> {
                User user = request.attribute(Application.USER_KEY);
                Customer customer = request.attribute(Application.CUSTOMER_KEY);
                List<Invoice> invoices = Invoice.getByCustomer(customer);
                FreemarkerContext context = new FreemarkerContext();
                context.put("title", "Customers");
                context.put("user", user);
                context.put("customer", customer);
                context.put("invoices", invoices);
                return Application.engine.render(context, "customer.ftl");
            });

            // Delete Customer
            get("/customer/:id/delete/", (request, response) -> {
                Customer customer = request.attribute(Application.CUSTOMER_KEY);
                // Remove all customer Invoices
                Invoice.getByCustomer(customer).forEach(Invoice::delete);
                customer.delete();
                response.redirect("/app/");
                return "";
            });

            // Edit Customer
            get("/customer/:id/edit/", (request, response) -> {
                User user = request.attribute(Application.USER_KEY);
                Customer customer = request.attribute(Application.CUSTOMER_KEY);
                FreemarkerContext context = new FreemarkerContext();
                context.put("title", "Customer Edit");
                context.put("user", user);
                context.put("customer", customer);
                return Application.engine.render(context, "customerEdit.ftl");
            });

            // Edit Customer Action
            post("/customer/:id/edit/", (request, response) -> {
                Customer customer = request.attribute(Application.CUSTOMER_KEY);
                String name = request.queryParams("name");
                String email = request.queryParams("email");
                if (name != null && !name.isBlank())
                    customer.name = name;
                if (email != null && !email.isBlank())
                    customer.email = email;
                customer.save();
                response.redirect("/app/");
                return "";
            });

            // Creates new Invoice
            post("/customer/:id/invoice/", (request, response) -> {
                Customer customer = request.attribute(Application.CUSTOMER_KEY);
                String date = request.queryParams("date");
                String amount = request.queryParams("amount");
                double amountDouble = 0;
                try {
                    amountDouble = Double.parseDouble(amount);
                } catch (NumberFormatException e) {
                    Application.sendError(HttpStatus.Code.BAD_REQUEST);
                }
                Invoice invoice = new Invoice();
                invoice.customer_id = customer.id;
                invoice.date = date;
                invoice.amount = amountDouble;
                invoice.complete = 0;
                invoice.save();
                response.redirect("/app/customer/" + customer.id + "/");
                return "";
            });

            // Intercept all requests to Invoice to verify if the customer id is one of the logged user
            before("/invoice/:id/*", (request, response) -> {
                User user = request.attribute(Application.USER_KEY);
                String id = request.params(":id");
                int idInt = 0;
                try {
                    idInt = Integer.parseInt(id);
                } catch (NumberFormatException e) {
                    Application.sendError(HttpStatus.Code.BAD_REQUEST);
                }
                Invoice invoice = Invoice.get(idInt);
                if (invoice == null) {
                    Application.sendError(HttpStatus.Code.NOT_FOUND);
                }
                else {
                    Customer customer = Customer.get(invoice.customer_id);
                    if (customer.user_id != user.id) {
                        Application.sendError(HttpStatus.Code.FORBIDDEN);
                    }
                    else {
                        request.attribute(Application.CUSTOMER_KEY, customer);
                        request.attribute(Application.INVOICE_KEY, invoice);
                    }
                }
            });

            // Delete Invoice
            get("/invoice/:id/delete/", (request, response) -> {
                Customer customer = request.attribute(Application.CUSTOMER_KEY);
                Invoice invoice = request.attribute(Application.INVOICE_KEY);
                invoice.delete();
                response.redirect("/app/customer/" + customer.id + "/");
                return "";
            });

            // Complete Invoice
            get("/invoice/:id/complete/", (request, response) -> {
                Customer customer = request.attribute(Application.CUSTOMER_KEY);
                Invoice invoice = request.attribute(Application.INVOICE_KEY);
                invoice.complete = 1;
                invoice.save();
                response.redirect("/app/customer/" + customer.id + "/");
                return "";
            });

            // Edit Invoice
            get("/invoice/:id/edit/", (request, response) -> {
                User user = request.attribute(Application.USER_KEY);
                Customer customer = request.attribute(Application.CUSTOMER_KEY);
                Invoice invoice = request.attribute(Application.INVOICE_KEY);
                FreemarkerContext context = new FreemarkerContext();
                context.put("title", "Customer Edit");
                context.put("user", user);
                context.put("customer", customer);
                context.put("invoice", invoice);
                return Application.engine.render(context, "invoiceEdit.ftl");
            });

            // Edit Invoice Action
            post("/invoice/:id/edit/", (request, response) -> {
                Customer customer = request.attribute(Application.CUSTOMER_KEY);
                Invoice invoice = request.attribute(Application.INVOICE_KEY);
                if (invoice.complete == 1) {
                    Application.sendError(HttpStatus.Code.FORBIDDEN);
                }
                String date = request.queryParams("date");
                String amount = request.queryParams("amount");
                double amountDouble = 0;
                try {
                    amountDouble = Double.parseDouble(amount);
                } catch (NumberFormatException e) {
                    Application.sendError(HttpStatus.Code.BAD_REQUEST);
                }
                if (date != null && !date.isBlank())
                    invoice.date = date;
                invoice.amount = amountDouble;
                invoice.save();
                response.redirect("/app/customer/" + customer.id + "/");
                return "";
            });

        });
    }
}
