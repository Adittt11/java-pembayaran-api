package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import utils.DatabaseUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class BaseHandler implements HttpHandler {
    private static final String API_KEY = "TEST"; // Change to your actual API key

    protected Connection connect() throws IOException {
        try {
            System.out.println("Connecting to database...");
            return DatabaseUtil.getConnection();
        } catch (SQLException e) {
            throw new IOException("Failed to connect to the database", e);
        }
    }

    protected void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        System.out.println("Sending response: " + response);
        exchange.sendResponseHeaders(statusCode, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    protected boolean authenticate(HttpExchange exchange) {
        String apiKey = exchange.getRequestHeaders().getFirst("API-Key");
        System.out.println("Authenticating API key: " + apiKey);
        return API_KEY.equals(apiKey);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!authenticate(exchange)) {
            sendResponse(exchange, 403, "Forbidden");
            return;
        }

        try (Connection connection = connect()) {
            handleRequest(exchange, connection);
        } catch (SQLException e) {
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }

    protected abstract void handleRequest(HttpExchange exchange, Connection connection) throws IOException, SQLException;
}
