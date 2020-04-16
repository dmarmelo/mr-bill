package dev.estgp.is;

import dev.estgp.is.api.Api;
import dev.estgp.is.app.App;
import dev.estgp.is.utils.freemarker.FreemarkerContext;
import dev.estgp.is.utils.freemarker.FreemarkerEngine;
import dev.estgp.is.utils.sqlite3.SQLiteConn;

import static spark.Spark.*;

/**
 * Simple Spark web application
 */
public class Application {

    public static FreemarkerEngine engine = null;

    public static final String USER_KEY = "user.key";
    public static final String CUSTOMER_KEY = "customer.key";
    public static final String INVOICE_KEY = "invoice.key";

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

        // If in development mode set the static files directory and the Freemarker templates directory to the relative path of the files
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
        conn.recreate("data/database.sql");

        // Define application Routes
        App.defineRoutes();

        // Define API Routes
        Api.defineRoutes();

        // Errors
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
