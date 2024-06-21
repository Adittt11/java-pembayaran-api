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

public class SubscriptionHandler extends BaseHandler {

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
                // GET /subscriptions
                String query = "SELECT * FROM subscriptions";
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();
                JSONArray subscriptions = new JSONArray();
                while (rs.next()) {
                    JSONObject subscription = new JSONObject();
                    subscription.put("id", rs.getInt("id"));
                    subscription.put("customer", rs.getInt("customer"));
                    subscription.put("billing_period", rs.getInt("billing_period"));
                    subscription.put("billing_period_unit", rs.getString("billing_period_unit"));
                    subscription.put("total_due", rs.getInt("total_due"));
                    subscription.put("activated_at", rs.getString("activated_at"));
                    subscription.put("current_term_start", rs.getString("current_term_start"));
                    subscription.put("current_term_end", rs.getString("current_term_end"));
                    subscription.put("status", rs.getString("status"));
                    subscriptions.put(subscription);
                }
                sendResponse(exchange, 200, subscriptions.toString());
            } else if (segments.length == 3) {
                int subscriptionId = Integer.parseInt(segments[2]);
                // GET /subscriptions/{id}
                String query = "SELECT * FROM subscriptions WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, subscriptionId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    JSONObject subscription = new JSONObject();
                    subscription.put("id", rs.getInt("id"));
                    subscription.put("customer", rs.getInt("customer"));
                    subscription.put("billing_period", rs.getInt("billing_period"));
                    subscription.put("billing_period_unit", rs.getString("billing_period_unit"));
                    subscription.put("total_due", rs.getInt("total_due"));
                    subscription.put("activated_at", rs.getString("activated_at"));
                    subscription.put("current_term_start", rs.getString("current_term_start"));
                    subscription.put("current_term_end", rs.getString("current_term_end"));
                    subscription.put("status", rs.getString("status"));

                    // Get customer details
                    String customerQuery = "SELECT id, first_name, last_name FROM customers WHERE id = ?";
                    PreparedStatement customerStmt = conn.prepareStatement(customerQuery);
                    customerStmt.setInt(1, rs.getInt("customer"));
                    ResultSet customerRs = customerStmt.executeQuery();
                    if (customerRs.next()) {
                        JSONObject customer = new JSONObject();
                        customer.put("id", customerRs.getInt("id"));
                        customer.put("first_name", customerRs.getString("first_name"));
                        customer.put("last_name", customerRs.getString("last_name"));
                        subscription.put("customer_details", customer);
                    }

                    // Get subscription items
                    String itemsQuery = "SELECT * FROM subscription_items WHERE subscription = ?";
                    PreparedStatement itemsStmt = conn.prepareStatement(itemsQuery);
                    itemsStmt.setInt(1, subscriptionId);
                    ResultSet itemsRs = itemsStmt.executeQuery();
                    JSONArray items = new JSONArray();
                    while (itemsRs.next()) {
                        JSONObject item = new JSONObject();
                        item.put("subscription", itemsRs.getInt("subscription"));
                        item.put("item", itemsRs.getInt("item"));
                        item.put("quantity", itemsRs.getInt("quantity"));
                        item.put("price", itemsRs.getInt("price"));
                        item.put("amount", itemsRs.getInt("amount"));
                        items.put(item);
                    }
                    subscription.put("subscription_items", items);
                    sendResponse(exchange, 200, subscription.toString());
                } else {
                    sendResponse(exchange, 404, "Subscription not found");
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

        JSONObject subscriptionJson = new JSONObject(requestBody.toString());

        try (Connection conn = connect()) {
            String query = "INSERT INTO subscriptions (customer, billing_period, billing_period_unit, total_due, activated_at, current_term_start, current_term_end, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, subscriptionJson.getInt("customer"));
            stmt.setInt(2, subscriptionJson.getInt("billing_period"));
            stmt.setString(3, subscriptionJson.getString("billing_period_unit"));
            stmt.setInt(4, subscriptionJson.getInt("total_due"));
            stmt.setString(5, subscriptionJson.getString("activated_at"));
            stmt.setString(6, subscriptionJson.getString("current_term_start"));
            stmt.setString(7, subscriptionJson.getString("current_term_end"));
            stmt.setString(8, subscriptionJson.getString("status"));
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                sendResponse(exchange, 201, "Subscription created successfully");
            } else {
                sendResponse(exchange, 400, "Failed to create subscription");
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

        int subscriptionId = Integer.parseInt(segments[2]);

        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }

        JSONObject subscriptionJson = new JSONObject(requestBody.toString());

        try (Connection conn = connect()) {
            String query = "UPDATE subscriptions SET customer = ?, billing_period = ?, billing_period_unit = ?, total_due = ?, activated_at = ?, current_term_start = ?, current_term_end = ?, status = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, subscriptionJson.getInt("customer"));
            stmt.setInt(2, subscriptionJson.getInt("billing_period"));
            stmt.setString(3, subscriptionJson.getString("billing_period_unit"));
            stmt.setInt(4, subscriptionJson.getInt("total_due"));
            stmt.setString(5, subscriptionJson.getString("activated_at"));
            stmt.setString(6, subscriptionJson.getString("current_term_start"));
            stmt.setString(7, subscriptionJson.getString("current_term_end"));
            stmt.setString(8, subscriptionJson.getString("status"));
            stmt.setInt(9, subscriptionId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                sendResponse(exchange, 200, "Subscription updated successfully");
            } else {
                sendResponse(exchange, 404, "Subscription not found");
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

        int subscriptionId = Integer.parseInt(segments[2]);

        try (Connection conn = connect()) {
            String query = "DELETE FROM subscriptions WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, subscriptionId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                sendResponse(exchange, 200, "Subscription deleted successfully");
            } else {
                sendResponse(exchange, 404, "Subscription not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }
}
