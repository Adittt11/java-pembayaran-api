package handlers;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.DatabaseUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemHandler extends BaseHandler {

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        switch (method) {
            case "GET":
                handleGet(exchange);
                break;
            case "POST":
                handlePost(exchange);
                break;
            case "PUT":
                handlePut(exchange);
                break;
            case "DELETE":
                handleDelete(exchange);
                break;
            default:
                sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] segments = path.split("/");
        try (Connection conn = connect()) {
            if (segments.length == 2) {
                // GET /items
                String query = "SELECT * FROM items";
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();
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
            } else if (segments.length == 3) {
                int itemId = Integer.parseInt(segments[2]);
                // GET /items/{id}
                String query = "SELECT * FROM items WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, itemId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    JSONObject item = new JSONObject();
                    item.put("id", rs.getInt("id"));
                    item.put("name", rs.getString("name"));
                    item.put("price", rs.getInt("price"));
                    item.put("type", rs.getString("type"));
                    item.put("is_active", rs.getInt("is_active"));
                    sendResponse(exchange, 200, item.toString());
                } else {
                    sendResponse(exchange, 404, "Item not found");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }

        JSONObject itemJson = new JSONObject(requestBody.toString());

        try (Connection conn = connect()) {
            String query = "INSERT INTO items (name, price, type, is_active) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, itemJson.getString("name"));
            stmt.setInt(2, itemJson.getInt("price"));
            stmt.setString(3, itemJson.getString("type"));
            stmt.setInt(4, itemJson.getInt("is_active"));
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                sendResponse(exchange, 201, "Item created successfully");
            } else {
                sendResponse(exchange, 400, "Failed to create item");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }

    private void handlePut(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] segments = path.split("/");
        if (segments.length != 3) {
            sendResponse(exchange, 400, "Invalid URL");
            return;
        }

        int itemId = Integer.parseInt(segments[2]);

        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }

        JSONObject itemJson = new JSONObject(requestBody.toString());

        try (Connection conn = connect()) {
            String query = "UPDATE items SET name = ?, price = ?, type = ?, is_active = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, itemJson.getString("name"));
            stmt.setInt(2, itemJson.getInt("price"));
            stmt.setString(3, itemJson.getString("type"));
            stmt.setInt(4, itemJson.getInt("is_active"));
            stmt.setInt(5, itemId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                sendResponse(exchange, 200, "Item updated successfully");
            } else {
                sendResponse(exchange, 404, "Item not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] segments = path.split("/");
        if (segments.length != 3) {
            sendResponse(exchange, 400, "Invalid URL");
            return;
        }

        int itemId = Integer.parseInt(segments[2]);

        try (Connection conn = connect()) {
            String query = "DELETE FROM items WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, itemId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                sendResponse(exchange, 200, "Item deleted successfully");
            } else {
                sendResponse(exchange, 404, "Item not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }
}
