package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import Main;

public abstract class BaseHandler implements HttpHandler {

    protected abstract void handleRequest(HttpExchange exchange) throws IOException;

    protected boolean authenticate(HttpExchange exchange) {
        String apiKey = exchange.getRequestHeaders().getFirst("API-Key");
        return Main.isAuthenticated(apiKey);
    }

    protected void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!authenticate(exchange)) {
            sendResponse(exchange, 403, "Forbidden");
            return;
        }
        handleRequest(exchange);
    }
}
