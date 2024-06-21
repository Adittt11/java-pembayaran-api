package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import utils.DatabaseUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;

public abstract class BaseHandler implements HttpHandler {
    private static final String API_KEY = "your-hardcoded-api-key"; // Change to your actual API key

    protected Connection connect() throws IOException {
        return DatabaseUtil.getConnection();
    }

    protected void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    protected boolean authenticate(HttpExchange exchange) {
        String apiKey = exchange.getRequestHeaders().getFirst("API-Key");
        return API_KEY.equals(apiKey);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!authenticate(exchange)) {
            sendResponse(exchange, 403, "Forbidden");
            return;
        }

        handleRequest(exchange);
    }

    protected abstract void handleRequest(HttpExchange exchange) throws IOException;
}
