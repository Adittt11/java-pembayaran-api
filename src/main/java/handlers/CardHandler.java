package handlers;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class CardHandler extends BaseHandler {
    @Override
    protected void handleRequest(HttpExchange exchange, Connection connection) throws IOException, SQLException {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET":
                handleGetRequest(exchange, connection);
                break;
            case "POST":
                handlePostRequest(exchange, connection);
                break;
            case "PUT":
                handlePutRequest(exchange, connection);
                break;
            case "DELETE":
                handleDeleteRequest(exchange, connection);
                break;
            default:
                sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    private void handleGetRequest(HttpExchange exchange, Connection connection) throws IOException {
        // Implement GET logic here
    }

    private void handlePostRequest(HttpExchange exchange, Connection connection) throws IOException {
        // Implement POST logic here
    }

    private void handlePutRequest(HttpExchange exchange, Connection connection) throws IOException {
        // Implement PUT logic here
    }

    private void handleDeleteRequest(HttpExchange exchange, Connection connection) throws IOException {
        // Implement DELETE logic here
    }
}
