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

public class CardHandler extends BaseHandler {

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
                // GET /cards
                String query = "SELECT * FROM cards";
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();
                JSONArray cards = new JSONArray();
                while (rs.next()) {
                    JSONObject card = new JSONObject();
                    card.put("id", rs.getInt("id"));
                    card.put("customer", rs.getInt("customer"));
                    card.put("card_number", rs.getString("card_number"));
                    card.put("expiry_month", rs.getString("expiry_month"));
                    card.put("expiry_year", rs.getString("expiry_year"));
                    card.put("cvv", rs.getString("cvv"));
                    card.put("billing_address", rs.getString("billing_address"));
                    card.put("is_default", rs.getBoolean("is_default"));
                    cards.put(card);
                }
                sendResponse(exchange, 200, cards.toString());
            } else if (segments.length == 3) {
                int cardId = Integer.parseInt(segments[2]);
                // GET /cards/{id}
                String query = "SELECT * FROM cards WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, cardId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    JSONObject card = new JSONObject();
                    card.put("id", rs.getInt("id"));
                    card.put("customer", rs.getInt("customer"));
                    card.put("card_number", rs.getString("card_number"));
                    card.put("expiry_month", rs.getString("expiry_month"));
                    card.put("expiry_year", rs.getString("expiry_year"));
                    card.put("cvv", rs.getString("cvv"));
                    card.put("billing_address", rs.getString("billing_address"));
                    card.put("is_default", rs.getBoolean("is_default"));
                    sendResponse(exchange, 200, card.toString());
                } else {
                    sendResponse(exchange, 404, "Card not found");
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

        JSONObject cardJson = new JSONObject(requestBody.toString());

        try (Connection conn = connect()) {
            String query = "INSERT INTO cards (customer, card_number, expiry_month, expiry_year, cvv, billing_address, is_default) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, cardJson.getInt("customer"));
            stmt.setString(2, cardJson.getString("card_number"));
            stmt.setString(3, cardJson.getString("expiry_month"));
            stmt.setString(4, cardJson.getString("expiry_year"));
            stmt.setString(5, cardJson.getString("cvv"));
            stmt.setString(6, cardJson.getString("billing_address"));
            stmt.setBoolean(7, cardJson.getBoolean("is_default"));
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                sendResponse(exchange, 201, "Card created successfully");
            } else {
                sendResponse(exchange, 400, "Failed to create card");
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

        int cardId = Integer.parseInt(segments[2]);

        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }

        JSONObject cardJson = new JSONObject(requestBody.toString());

        try (Connection conn = connect()) {
            String query = "UPDATE cards SET customer = ?, card_number = ?, expiry_month = ?, expiry_year = ?, cvv = ?, billing_address = ?, is_default = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, cardJson.getInt("customer"));
            stmt.setString(2, cardJson.getString("card_number"));
            stmt.setString(3, cardJson.getString("expiry_month"));
            stmt.setString(4, cardJson.getString("expiry_year"));
            stmt.setString(5, cardJson.getString("cvv"));
            stmt.setString(6, cardJson.getString("billing_address"));
            stmt.setBoolean(7, cardJson.getBoolean("is_default"));
            stmt.setInt(8, cardId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                sendResponse(exchange, 200, "Card updated successfully");
            } else {
                sendResponse(exchange, 404, "Card not found");
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

        int cardId = Integer.parseInt(segments[2]);

        try (Connection conn = connect()) {
            String query = "DELETE FROM cards WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, cardId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                sendResponse(exchange, 200, "Card deleted successfully");
            } else {
                sendResponse(exchange, 404, "Card not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }
}
