package handlers;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemHandler extends BaseHandler {
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
        URI requestURI = exchange.getRequestURI();
        String path = requestURI.getPath();
        String query = requestURI.getQuery();

        try {
            if (path.equals("/items")) {
                if (query != null && query.contains("is_active=true")) {
                    handleGetActiveItems(exchange, connection);
                } else {
                    handleGetAllItems(exchange, connection);
                }
            } else if (path.matches("/items/\\d+")) {
                handleGetItemById(exchange, connection, path.split("/")[2]);
            } else {
                sendResponse(exchange, 404, "Not Found");
            }
        } catch (SQLException e) {
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleGetAllItems(HttpExchange exchange, Connection connection) throws SQLException, IOException {
        String query = "SELECT * FROM items";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            JSONArray items = new JSONArray();

            while (rs.next()) {
                JSONObject item = new JSONObject();
                item.put("id", rs.getInt("id"));
                item.put("name", rs.getString("name"));
                item.put("price", rs.getInt("price"));
                item.put("type", rs.getString("type"));
                item.put("is_active", rs.getInt("is_active"));
                items.put(item);
            }

            sendResponse(exchange, 200, items.toString());
        }
    }

    private void handleGetActiveItems(HttpExchange exchange, Connection connection) throws SQLException, IOException {
        String query = "SELECT * FROM items WHERE is_active = 1";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            JSONArray items = new JSONArray();

            while (rs.next()) {
                JSONObject item = new JSONObject();
                item.put("id", rs.getInt("id"));
                item.put("name", rs.getString("name"));
                item.put("price", rs.getInt("price"));
                item.put("type", rs.getString("type"));
                item.put("is_active", rs.getInt("is_active"));
                items.put(item);
            }

            sendResponse(exchange, 200, items.toString());
        }
    }

    private void handleGetItemById(HttpExchange exchange, Connection connection, String id) throws SQLException, IOException {
        String query = "SELECT * FROM items WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(id));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    JSONObject item = new JSONObject();
                    item.put("id", rs.getInt("id"));
                    item.put("name", rs.getString("name"));
                    item.put("price", rs.getInt("price"));
                    item.put("type", rs.getString("type"));
                    item.put("is_active", rs.getInt("is_active"));

                    sendResponse(exchange, 200, item.toString());
                } else {
                    sendResponse(exchange, 404, "Item Not Found");
                }
            }
        }
    }

    protected void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
    
    private void handlePostRequest(HttpExchange exchange, Connection connection) throws IOException {
        if (exchange.getRequestURI().getPath().equals("/items")) {
            handleCreateItem(exchange, connection);
        } else {
            sendResponse(exchange, 404, "Not Found");
        }
    }

    private void handleCreateItem(HttpExchange exchange, Connection connection) throws IOException {
        try {
            JSONObject requestBody = new JSONObject(new String(exchange.getRequestBody().readAllBytes()));
            String name = requestBody.getString("name");
            int price = requestBody.getInt("price");
            String type = requestBody.getString("type");
            int isActive = requestBody.getInt("is_active");

            String query = "INSERT INTO items (name, price, type, is_active) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, name);
                stmt.setInt(2, price);
                stmt.setString(3, type);
                stmt.setInt(4, isActive);
                stmt.executeUpdate();
            }

            sendResponse(exchange, 201, "Item created successfully");
        } catch (SQLException e) {
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }


    private void handlePutRequest(HttpExchange exchange, Connection connection) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.matches("/items/\\d+")) {
            int itemId = Integer.parseInt(path.split("/")[2]);
            updateItem(exchange, connection, itemId);
        } else {
            sendResponse(exchange, 404, "Not Found");
        }
    }

    private void updateItem(HttpExchange exchange, Connection connection, int itemId) throws IOException {
        try {
            JSONObject requestBody = new JSONObject(new String(exchange.getRequestBody().readAllBytes()));
            String name = requestBody.getString("name");
            int price = requestBody.getInt("price");
            String type = requestBody.getString("type");
            int isActive = requestBody.getInt("is_active");

            String query = "UPDATE items SET name = ?, price = ?, type = ?, is_active = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, name);
                stmt.setInt(2, price);
                stmt.setString(3, type);
                stmt.setInt(4, isActive);
                stmt.setInt(5, itemId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    sendResponse(exchange, 200, "Item updated successfully");
                } else {
                    sendResponse(exchange, 404, "Item Not Found");
                }
            }
        } catch (SQLException e) {
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleDeleteRequest(HttpExchange exchange, Connection connection) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.matches("/items/\\d+")) {
            int itemId = Integer.parseInt(path.split("/")[2]);
            deleteItem(exchange, connection, itemId);
        } else {
            sendResponse(exchange, 404, "Not Found");
        }
    }

    private void deleteItem(HttpExchange exchange, Connection connection, int itemId) throws IOException {
        try {
            String query = "UPDATE items SET is_active = 0 WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, itemId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    sendResponse(exchange, 200, "Item deactivated successfully");
                } else {
                    sendResponse(exchange, 404, "Item Not Found");
                }
            }
        } catch (SQLException e) {
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
}
