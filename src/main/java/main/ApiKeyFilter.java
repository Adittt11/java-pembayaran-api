package main;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ApiKeyFilter extends Filter {

    private final String apiKey;

    public ApiKeyFilter(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String requestApiKey = getRequestApiKey(exchange);

        if (requestApiKey == null || !requestApiKey.equals(apiKey)) {
            sendUnauthorizedResponse(exchange);
            return;
        }

        chain.doFilter(exchange);
    }

    private String getRequestApiKey(HttpExchange exchange) {
        List<String> apiKeyHeaders = exchange.getRequestHeaders().get("API-Key");
        if (apiKeyHeaders != null && !apiKeyHeaders.isEmpty()) {
            return apiKeyHeaders.get(0);
        }
        return null;
    }

    private void sendUnauthorizedResponse(HttpExchange exchange) throws IOException {
        String response = "Unauthorized";
        exchange.sendResponseHeaders(401, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    @Override
    public String description() {
        return "API key filter";
    }
}
