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
import java.sql.Statement;

public class SubscriptionHandler extends BaseHandler {
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
            if (path.equals("/subscriptions")) {
                if (query != null && query.contains("sort_by=current_term_end") && query.contains("sort_type=desc")) {
                    handleGetSubscriptionsSorted(exchange, connection);
                } else {
                    handleGetAllSubscriptions(exchange, connection);
                }
            } else if (path.matches("/subscriptions/\\d+")) {
                handleGetSubscriptionById(exchange, connection, path.split("/")[2]);
            } else {
                sendResponse(exchange, 404, "Not Found");
            }
        } catch (SQLException e) {
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleGetAllSubscriptions(HttpExchange exchange, Connection connection) throws SQLException, IOException {
        String query = "SELECT * FROM subscriptions";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

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
        }
    }

    private void handleGetSubscriptionsSorted(HttpExchange exchange, Connection connection) throws SQLException, IOException {
        String query = "SELECT * FROM subscriptions ORDER BY current_term_end DESC";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

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
        }
    }

    private void handleGetSubscriptionById(HttpExchange exchange, Connection connection, String id) throws SQLException, IOException {
        String query = "SELECT s.id AS subscription_id, s.customer AS customer_id, s.billing_period, s.billing_period_unit, " +
                "s.total_due, s.activated_at, s.current_term_start, s.current_term_end, s.status, " +
                "c.first_name, c.last_name, si.quantity, si.amount, i.id AS item_id, i.name, i.price, i.type " +
                "FROM subscriptions s " +
                "JOIN customers c ON s.customer = c.id " +
                "LEFT JOIN subscription_items si ON s.id = si.subscription " +
                "LEFT JOIN items i ON si.item = i.id " +
                "WHERE s.id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(id));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    JSONObject subscription = new JSONObject();
                    subscription.put("id", rs.getInt("subscription_id"));
                    subscription.put("billing_period", rs.getInt("billing_period"));
                    subscription.put("billing_period_unit", rs.getString("billing_period_unit"));
                    subscription.put("total_due", rs.getInt("total_due"));
                    subscription.put("activated_at", rs.getString("activated_at"));
                    subscription.put("current_term_start", rs.getString("current_term_start"));
                    subscription.put("current_term_end", rs.getString("current_term_end"));
                    subscription.put("status", rs.getString("status"));

                    JSONObject customer = new JSONObject();
                    customer.put("id", rs.getInt("customer_id"));
                    customer.put("first_name", rs.getString("first_name"));
                    customer.put("last_name", rs.getString("last_name"));
                    subscription.put("customer", customer);

                    JSONArray subscriptionItems = new JSONArray();

                    do {
                        JSONObject subscriptionItem = new JSONObject();
                        subscriptionItem.put("quantity", rs.getInt("quantity"));
                        subscriptionItem.put("amount", rs.getInt("amount"));

                        JSONObject item = new JSONObject();
                        item.put("id", rs.getInt("item_id"));
                        item.put("name", rs.getString("name"));
                        item.put("price", rs.getInt("price"));
                        item.put("type", rs.getString("type"));

                        subscriptionItem.put("item", item);
                        subscriptionItems.put(subscriptionItem);
                    } while (rs.next());

                    subscription.put("subscription_items", subscriptionItems);

                    sendResponse(exchange, 200, subscription.toString());
                } else {
                    sendResponse(exchange, 404, "Subscription Not Found");
                }
            }
        }
    }

private void handlePostRequest(HttpExchange exchange, Connection connection) throws IOException {
    if (exchange.getRequestURI().getPath().equals("/subscriptions")) {
        handleCreateSubscription(exchange, connection);
    } else {
        sendResponse(exchange, 404, "Not Found");
    }
}

private void handleCreateSubscription(HttpExchange exchange, Connection connection) throws IOException {
    try {
        JSONObject requestBody = new JSONObject(new String(exchange.getRequestBody().readAllBytes()));
        int customerId = requestBody.getInt("customer_id");
        JSONObject shippingAddress = requestBody.getJSONObject("shipping_address");
        JSONObject card = requestBody.getJSONObject("card");
        JSONArray items = requestBody.getJSONArray("items");

        connection.setAutoCommit(false);

        // Inserting a new subscription
        String insertSubscriptionQuery = "INSERT INTO subscriptions (customer, billing_period, billing_period_unit, total_due, activated_at, current_term_start, current_term_end, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertSubscriptionQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, requestBody.getInt("billing_period"));
            stmt.setString(3, requestBody.getString("billing_period_unit"));
            stmt.setInt(4, requestBody.getInt("total_due"));
            stmt.setString(5, requestBody.getString("activated_at"));
            stmt.setString(6, requestBody.getString("current_term_start"));
            stmt.setString(7, requestBody.getString("current_term_end"));
            stmt.setString(8, requestBody.getString("status"));
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int subscriptionId = generatedKeys.getInt(1);

                // Inserting items for the new subscription
                String insertItemQuery = "INSERT INTO subscription_items (subscription, item, quantity, price, amount) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement itemStmt = connection.prepareStatement(insertItemQuery)) {
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        itemStmt.setInt(1, subscriptionId);
                        itemStmt.setInt(2, item.getInt("item_id"));
                        itemStmt.setInt(3, item.getInt("quantity"));
                        itemStmt.setInt(4, item.getInt("price"));
                        itemStmt.setInt(5, item.getInt("amount"));
                        itemStmt.addBatch();
                    }
                    itemStmt.executeBatch();
                }

                // Inserting or updating the shipping address
                String insertShippingAddressQuery = "INSERT INTO shipping_addresses (customer, title, line1, line2, city, province, postcode) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement addressStmt = connection.prepareStatement(insertShippingAddressQuery)) {
                    addressStmt.setInt(1, customerId);
                    addressStmt.setString(2, shippingAddress.getString("title"));
                    addressStmt.setString(3, shippingAddress.getString("line1"));
                    addressStmt.setString(4, shippingAddress.optString("line2"));
                    addressStmt.setString(5, shippingAddress.getString("city"));
                    addressStmt.setString(6, shippingAddress.getString("province"));
                    addressStmt.setString(7, shippingAddress.getString("postcode"));
                    addressStmt.executeUpdate();
                }

                // Inserting or updating the card details
                String insertCardQuery = "INSERT INTO cards (customer, card_type, masked_number, expiry_month, expiry_year, status, is_primary) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement cardStmt = connection.prepareStatement(insertCardQuery)) {
                    cardStmt.setInt(1, customerId);
                    cardStmt.setString(2, card.getString("card_type"));
                    cardStmt.setString(3, card.getString("masked_number"));
                    cardStmt.setInt(4, card.getInt("expiry_month"));
                    cardStmt.setInt(5, card.getInt("expiry_year"));
                    cardStmt.setString(6, card.getString("status"));
                    cardStmt.setInt(7, card.getInt("is_primary"));
                    cardStmt.executeUpdate();
                }
            }

            connection.commit();
            sendResponse(exchange, 201, "Subscription created successfully");
        } catch (SQLException e) {
            connection.rollback();
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        } finally {
            connection.setAutoCommit(true);
        }
    } catch (SQLException e) {
        sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
    }
}



    private void handlePutRequest(HttpExchange exchange, Connection connection) throws IOException {
        // Implement PUT logic here
    }

    private void handleDeleteRequest(HttpExchange exchange, Connection connection) throws IOException {
        // Implement DELETE logic here
    }
    
    protected void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
