package dev.estgp.is;

import com.google.gson.Gson;
import dev.estgp.is.models.Customer;
import dev.estgp.is.models.Invoice;
import dev.estgp.is.models.User;
import dev.estgp.is.utils.sqlite3.SQLiteConn;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the Spark application
 */
public class ApiTests {

    // The Http client
    private final HttpClient httpClient = HttpClient.newBuilder().build();

    // The application SQLite connection
    public static SQLiteConn conn = SQLiteConn.getSharedInstance();

    // Set up Gson
    Gson gson = new Gson();

    @BeforeAll
    public static void createServer() {
        String[] args = {};
        Application.main(args);
    }

    @AfterAll
    public static void killServer() {
        Spark.stop();
    }

    @BeforeEach
    public void clearDB() {
        conn.recreate("data/database.sql");
    }

    @Test
    // Users endpoint return 200 OK
    public void testUserListOk() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/api/users/"))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    // Check if the endpoint returns the correct list of all system users
    public void testUserListResults() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/api/users/"))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        String allUsers = gson.toJson(User.all());
        assertEquals(allUsers, response.body());
    }

    @Test
    // Customer endpoint return 200 OK
    public void testCustomerListAuthorizedUser() throws IOException, InterruptedException {
        User user = User.get(1);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/api/customers/?apiKey=" + user.apikey))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    // Customer endpoint return 401 OK
    public void testCustomerListUnauthorizedUser() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/api/customers/?apiKey=none"))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(401, response.statusCode());
    }

    @Test
    // Customer endpoint return 200 OK
    public void testCustomerOk() throws IOException, InterruptedException {
        User user = User.get(1);
        Customer customer = Customer.get(1);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/api/customers/" + customer.id + "/?apiKey=" + user.apikey))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        Customer res = gson.fromJson(response.body(), Customer.class);
        assertEquals(200, response.statusCode());
        assertEquals(customer, res);
    }

    @Test
    // Customer endpoint return 200 OK
    public void testCustomerNotOk() throws IOException, InterruptedException {
        User user = User.get(1);
        Customer customer = Customer.get(2);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/api/customers/" + 1 + "/?apiKey=" + user.apikey))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        Customer res = gson.fromJson(response.body(), Customer.class);
        assertEquals(200, response.statusCode());
        assertNotEquals(customer, res);
    }

    @Test
    // Customer endpoint return 403
    public void testUserDoesNotHaveAccessToCustomer() throws IOException, InterruptedException {
        User user = User.get(1);
        User otherUser = User.get(2);
        Customer customer = Customer.get(otherUser).get(0);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/api/customers/" + customer.id + "/?apiKey=" + user.apikey))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(403, response.statusCode());
    }

    @Test
    // Customer endpoint return 404
    public void testCustomerDoesNotExists() throws IOException, InterruptedException {
        User user = User.get(1);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/api/customers/" + Integer.MAX_VALUE + "/?apiKey=" + user.apikey))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    // Customer endpoint return 400
    public void testCustomerInvalidId() throws IOException, InterruptedException {
        User user = User.get(1);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/api/customers/thisisaninvalidid/?apiKey=" + user.apikey))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    // Customer endpoint really creates the customer in the database
    public void testCustomerCreate() throws IOException, InterruptedException {
        User user = User.get(1);
        Customer newCustomer = new Customer();
        newCustomer.name = "New Test Customer";
        newCustomer.email = "test@test.dev";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/api/customers/?apiKey=" + user.apikey))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(newCustomer)))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Customer customer = gson.fromJson(response.body(), Customer.class);

        assertEquals(201, response.statusCode());
        Customer actual = Customer.get(customer.id);
        assertNotNull(actual);
        assertEquals(user.id, actual.user_id);
    }

    @Test
    // Customer endpoint really deletes the customer from the database
    public void testCustomerDelete() throws IOException, InterruptedException {
        User user = User.get(1);
        Customer customer = Customer.get(1);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/api/customers/" + customer.id + "/?apiKey=" + user.apikey))
                .DELETE()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNull(Customer.get(customer.id));
    }

    @Test
    // Customer endpoint really updates the customer in the database
    public void testCustomerUpdate() throws IOException, InterruptedException {
        User user = User.get(1);
        Customer customer = Customer.get(1);
        customer.name = customer.name + " modify test";
        customer.email = customer.email + " modify test";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/api/customers/" + customer.id + "/?apiKey=" + user.apikey))
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(customer)))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Customer actual = Customer.get(customer.id);
        assertEquals(customer.name, actual.name);
        assertEquals(customer.email, actual.email);
    }

    @Test
    // Get User Invoices
    public void testCustomerInvoices() throws IOException, InterruptedException {
        User user = User.get(1);
        Customer customer = Customer.get(1);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/api/customers/" + customer.id + "/invoices/?apiKey=" + user.apikey))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    // Customer endpoint return 200 OK
    public void testCustomerInvoiceOk() throws IOException, InterruptedException {
        User user = User.get(1);
        Customer customer = Customer.get(1);
        Invoice invoice = Invoice.get(customer).get(0);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/api/customers/" + customer.id + "/invoices/" + invoice.id + "/?apiKey=" + user.apikey))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        Invoice res = gson.fromJson(response.body(), Invoice.class);
        assertEquals(200, response.statusCode());
        assertEquals(invoice, res);
    }

    @Test
    // Customer endpoint return 403
    public void testUserDoesNotHaveAccessToInvoice() throws IOException, InterruptedException {
        User user = User.get(1);
        Customer customer = Customer.get(user).get(0);
        User otherUser = User.get(2);
        Customer otherCustomer = Customer.get(otherUser).get(0);
        Invoice invoice = Invoice.get(otherCustomer).get(0);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/api/customers/" + customer.id + "/invoices/" + invoice.id + "/?apiKey=" + user.apikey))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(403, response.statusCode());
    }

    @Test
    // Customer endpoint return 404
    public void testInvoiceDoesNotExists() throws IOException, InterruptedException {
        User user = User.get(1);
        Customer customer = Customer.get(user).get(0);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/api/customers/" + customer.id + "/invoices/" + Integer.MAX_VALUE + "/?apiKey=" + user.apikey))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    // Customer endpoint return 400
    public void testInvoiceInvalidId() throws IOException, InterruptedException {
        User user = User.get(1);
        Customer customer = Customer.get(user).get(0);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/api/customers/" + customer.id + "/invoices/thisisanotherinvalidid/?apiKey=" + user.apikey))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    // Invoice endpoint really creates the invoice in the database
    public void testInvoiceCreate() throws IOException, InterruptedException {
        User user = User.get(1);
        Customer customer = Customer.get(user).get(0);
        Invoice newInvoice = new Invoice();
        newInvoice.date = "2020-04-17";
        newInvoice.amount = 19.99;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/api/customers/" + customer.id + "/invoices/?apiKey=" + user.apikey))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(newInvoice)))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Invoice invoice = gson.fromJson(response.body(), Invoice.class);

        assertEquals(201, response.statusCode());
        Invoice actual = Invoice.get(invoice.id);
        assertNotNull(actual);
        assertEquals(customer.id, actual.customer_id);
    }

    @Test
    // Invoice endpoint really deletes the invoice from the database
    public void testInvoiceDelete() throws IOException, InterruptedException {
        User user = User.get(1);
        Customer customer = Customer.get(user).get(0);
        Invoice invoice = Invoice.get(customer).get(0);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/api/customers/" + customer.id + "/invoices/" + invoice.id + "/?apiKey=" + user.apikey))
                .DELETE()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNull(Invoice.get(invoice.id));
    }

    @Test
    // Invoice endpoint really updates the invoice in the database
    public void testInvoiceUpdate() throws IOException, InterruptedException {
        User user = User.get(1);
        Customer customer = Customer.get(user).get(0);
        Invoice invoice = Invoice.get(customer).get(0);
        invoice.date = "2020-04-17";
        invoice.amount = 19.99;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/api/customers/" + customer.id + "/invoices/" + invoice.id + "/?apiKey=" + user.apikey))
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(invoice)))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Invoice actual = Invoice.get(invoice.id);
        assertEquals(invoice.date, actual.date);
        assertEquals(invoice.amount, actual.amount);
    }

    @Test
    // Invoice endpoint really completes the invoice in the database
    public void testInvoiceComplete() throws IOException, InterruptedException {
        User user = User.get(1);
        Customer customer = Customer.get(user).get(0);
        Invoice invoice = Invoice.get(customer).stream().filter(i -> i.complete == 0).findFirst().get();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/api/customers/" + customer.id + "/invoices/" + invoice.id + "/complete/?apiKey=" + user.apikey))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Invoice actual = Invoice.get(invoice.id);
        assertEquals(1, actual.complete);
    }
}
