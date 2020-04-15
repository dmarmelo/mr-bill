package dev.estgp.is;

import com.google.gson.Gson;
import dev.estgp.is.models.Customer;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Tests the Spark application
 */
public class Tests {

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
    // Customer endpoint return 200 OK
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
        assertEquals(res, customer);
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
        assertNotEquals(res, customer);
    }
}
