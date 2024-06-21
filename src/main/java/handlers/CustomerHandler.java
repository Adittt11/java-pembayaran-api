package handlers;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerHandler extends BaseHandler {
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

    private void handleGetRequest(HttpExchange exchange, Connection connection) throws IOException, SQLException {
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        System.out.println("Path: " + path);
        System.out.println("Query: " + query);

        if (path.equals("/customers")) {
            getAllCustomers(exchange, connection);
        } else if (path.matches("/customers/\\d+")) {
            int customerId = Integer.parseInt(path.split("/")[2]);
            getCustomerById(exchange, connection, customerId);
        } else if (path.matches("/customers/\\d+/cards")) {
            int customerId = Integer.parseInt(path.split("/")[2]);
            getCustomerCards(exchange, connection, customerId);
        } else if (path.matches("/customers/\\d+/subscriptions")) {
            int customerId = Integer.parseInt(path.split("/")[2]);
            if (query != null && query.startsWith("subscriptions_status=")) {
                String status = query.split("=")[1];
                getCustomerSubscriptionsByStatus(exchange, connection, customerId, status);
            } else {
                getCustomerSubscriptions(exchange, connection, customerId);
            }
        } else {
            sendResponse(exchange, 404, "Not Found");
        }
    }

    private void getAllCustomers(HttpExchange exchange, Connection connection) throws IOException, SQLException {
        String query = "SELECT * FROM customers";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            JSONArray customers = new JSONArray();
            while (rs.next()) {
                JSONObject customer = new JSONObject();
                customer.put("id", rs.getInt("id"));
                customer.put("email", rs.getString("email"));
                customer.put("first_name", rs.getString("first_name"));
                customer.put("last_name", rs.getString("last_name"));
                customer.put("phone_number", rs.getString("phone_number"));
                customers.put(customer);
            }

            String jsonResponse = customers.toString(4); // Pretty print with indent 4
            System.out.println(jsonResponse);  // Log JSON
            sendResponse(exchange, 200, jsonResponse);
        }
    }

    private void getCustomerById(HttpExchange exchange, Connection connection, int customerId) throws IOException, SQLException {
        String customerQuery = "SELECT * FROM customers WHERE id = ?";
        String addressQuery = "SELECT * FROM shipping_addresses WHERE customer = ? ORDER BY id";

        try (PreparedStatement customerStmt = connection.prepareStatement(customerQuery);
             PreparedStatement addressStmt = connection.prepareStatement(addressQuery)) {
            customerStmt.setInt(1, customerId);
            addressStmt.setInt(1, customerId);

            try (ResultSet customerRs = customerStmt.executeQuery();
                 ResultSet addressRs = addressStmt.executeQuery()) {
                if (customerRs.next()) {
                    JSONObject customer = new JSONObject();
                    customer.put("id", customerRs.getInt("id"));
                    customer.put("email", customerRs.getString("email"));
                    customer.put("first_name", customerRs.getString("first_name"));
                    customer.put("last_name", customerRs.getString("last_name"));
                    customer.put("phone_number", customerRs.getString("phone_number"));

                    JSONArray addresses = new JSONArray();
                    while (addressRs.next()) {
                        JSONObject address = new JSONObject();
                        address.put("id", addressRs.getInt("id"));
                        address.put("title", addressRs.getString("title"));
                        address.put("line1", addressRs.getString("line1"));
                        address.put("line2", addressRs.getString("line2"));
                        address.put("city", addressRs.getString("city"));
                        address.put("province", addressRs.getString("province"));
                        address.put("postcode", addressRs.getString("postcode"));
                        addresses.put(address);
                    }
                    customer.put("addresses", addresses);

                    String jsonResponse = customer.toString(4); // Pretty print with indent 4
                    System.out.println(jsonResponse);  // Log JSON
                    sendResponse(exchange, 200, jsonResponse);
                } else {
                    sendResponse(exchange, 404, "Customer Not Found");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Print stack trace for debugging
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void getCustomerCards(HttpExchange exchange, Connection connection, int customerId) throws IOException, SQLException {
        String query = "SELECT * FROM cards WHERE customer = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                JSONArray cards = new JSONArray();
                while (rs.next()) {
                    JSONObject card = new JSONObject();
                    card.put("id", rs.getInt("id"));
                    card.put("card_type", rs.getString("card_type"));
                    card.put("masked_number", rs.getString("masked_number"));
                    card.put("expiry_month", rs.getInt("expiry_month"));
                    card.put("expiry_year", rs.getInt("expiry_year"));
                    card.put("status", rs.getString("status"));
                    card.put("is_primary", rs.getInt("is_primary"));
                    cards.put(card);
                }

                String jsonResponse = cards.toString(4); // Pretty print with indent 4
                System.out.println(jsonResponse);  // Log JSON
                sendResponse(exchange, 200, jsonResponse);
            }
        }
    }

    private void getCustomerSubscriptions(HttpExchange exchange, Connection connection, int customerId) throws IOException, SQLException {
        String query = "SELECT * FROM subscriptions WHERE customer = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                JSONArray subscriptions = new JSONArray();
                while (rs.next()) {
                    JSONObject subscription = new JSONObject();
                    subscription.put("id", rs.getInt("id"));
                    subscription.put("billing_period", rs.getInt("billing_period"));
                    subscription.put("billing_period_unit", rs.getString("billing_period_unit"));
                    subscription.put("total_due", rs.getInt("total_due"));
                    subscription.put("activated_at", rs.getString("activated_at"));
                    subscription.put("current_term_start", rs.getString("current_term_start"));
                    subscription.put("current_term_end", rs.getString("current_term_end"));
                    subscription.put("status", rs.getString("status"));
                    subscriptions.put(subscription);
                }

                String jsonResponse = subscriptions.toString(4); // Pretty print with indent 4
                System.out.println(jsonResponse);  // Log JSON
                sendResponse(exchange, 200, jsonResponse);
            }
        }
    }

    private void getCustomerSubscriptionsByStatus(HttpExchange exchange, Connection connection, int customerId, String status) throws IOException, SQLException {
        String query = "SELECT * FROM subscriptions WHERE customer = ? AND status = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            stmt.setString(2, status);
            try (ResultSet rs = stmt.executeQuery()) {
                JSONArray subscriptions = new JSONArray();
                while (rs.next()) {
                    JSONObject subscription = new JSONObject();
                    subscription.put("id", rs.getInt("id"));
                    subscription.put("billing_period", rs.getInt("billing_period"));
                    subscription.put("billing_period_unit", rs.getString("billing_period_unit"));
                    subscription.put("total_due", rs.getInt("total_due"));
                    subscription.put("activated_at", rs.getString("activated_at"));
                    subscription.put("current_term_start", rs.getString("current_term_start"));
                    subscription.put("current_term_end", rs.getString("current_term_end"));
                    subscription.put("status", rs.getString("status"));
                    subscriptions.put(subscription);
                }

                String jsonResponse = subscriptions.toString(4); // Pretty print with indent 4
                System.out.println(jsonResponse);  // Log JSON
                sendResponse(exchange, 200, jsonResponse);
            }
        }
    }

    private void handlePostRequest(HttpExchange exchange, Connection connection) throws IOException {
        if (exchange.getRequestURI().getPath().equals("/customers")) {
            handleCreateCustomer(exchange, connection);
        } else {
            sendResponse(exchange, 404, "Not Found");
        }
    }

    private void handleCreateCustomer(HttpExchange exchange, Connection connection) throws IOException {
        try {
            JSONObject requestBody = new JSONObject(new String(exchange.getRequestBody().readAllBytes()));
            String email = requestBody.getString("email");
            String firstName = requestBody.getString("first_name");
            String lastName = requestBody.getString("last_name");
            String phoneNumber = requestBody.getString("phone_number");

            String query = "INSERT INTO customers (email, first_name, last_name, phone_number) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, email);
                stmt.setString(2, firstName);
                stmt.setString(3, lastName);
                stmt.setString(4, phoneNumber);
                stmt.executeUpdate();
            }

            sendResponse(exchange, 201, "Customer created successfully");
        } catch (SQLException e) {
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }


    private void handlePutRequest(HttpExchange exchange, Connection connection) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        if (path.matches("/customers/\\d+")) {
            int customerId = Integer.parseInt(pathParts[2]);
            updateCustomer(exchange, connection, customerId);
        } else if (path.matches("/customers/\\d+/shipping_addresses/\\d+")) {
            int customerId = Integer.parseInt(pathParts[2]);
            int addressId = Integer.parseInt(pathParts[4]);
            updateShippingAddress(exchange, connection, customerId, addressId);
        } else {
            sendResponse(exchange, 404, "Not Found");
        }
    }

    private void updateCustomer(HttpExchange exchange, Connection connection, int customerId) throws IOException {
        try {
            JSONObject requestBody = new JSONObject(new String(exchange.getRequestBody().readAllBytes()));
            String email = requestBody.getString("email");
            String firstName = requestBody.getString("first_name");
            String lastName = requestBody.getString("last_name");
            String phoneNumber = requestBody.getString("phone_number");

            String query = "UPDATE customers SET email = ?, first_name = ?, last_name = ?, phone_number = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, email);
                stmt.setString(2, firstName);
                stmt.setString(3, lastName);
                stmt.setString(4, phoneNumber);
                stmt.setInt(5, customerId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    sendResponse(exchange, 200, "Customer updated successfully");
                } else {
                    sendResponse(exchange, 404, "Customer Not Found");
                }
            }
        } catch (SQLException e) {
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void updateShippingAddress(HttpExchange exchange, Connection connection, int customerId, int addressId) throws IOException {
        try {
            JSONObject requestBody = new JSONObject(new String(exchange.getRequestBody().readAllBytes()));
            String title = requestBody.getString("title");
            String line1 = requestBody.getString("line1");
            String line2 = requestBody.optString("line2", "");
            String city = requestBody.getString("city");
            String province = requestBody.getString("province");
            String postcode = requestBody.getString("postcode");

            String query = "UPDATE shipping_addresses SET title = ?, line1 = ?, line2 = ?, city = ?, province = ?, postcode = ? WHERE id = ? AND customer = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, title);
                stmt.setString(2, line1);
                stmt.setString(3, line2);
                stmt.setString(4, city);
                stmt.setString(5, province);
                stmt.setString(6, postcode);
                stmt.setInt(7, addressId);
                stmt.setInt(8, customerId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    sendResponse(exchange, 200, "Shipping address updated successfully");
                } else {
                    sendResponse(exchange, 404, "Shipping address or Customer Not Found");
                }
            }
        } catch (SQLException e) {
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleDeleteRequest(HttpExchange exchange, Connection connection) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        if (path.matches("/customers/\\d+/cards/\\d+")) {
            int customerId = Integer.parseInt(pathParts[2]);
            int cardId = Integer.parseInt(pathParts[4]);
            deleteCustomerCard(exchange, connection, customerId, cardId);
        } else {
            sendResponse(exchange, 404, "Not Found");
        }
    }

    private void deleteCustomerCard(HttpExchange exchange, Connection connection, int customerId, int cardId) throws IOException {
        try {
            String checkPrimaryQuery = "SELECT is_primary FROM cards WHERE id = ? AND customer = ?";
            String deleteQuery = "DELETE FROM cards WHERE id = ? AND customer = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkPrimaryQuery);
                 PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
                checkStmt.setInt(1, cardId);
                checkStmt.setInt(2, customerId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    if (rs.getInt("is_primary") == 0) {
                        deleteStmt.setInt(1, cardId);
                        deleteStmt.setInt(2, customerId);
                        int rowsAffected = deleteStmt.executeUpdate();
                        if (rowsAffected > 0) {
                            sendResponse(exchange, 200, "Card deleted successfully");
                        } else {
                            sendResponse(exchange, 404, "Card Not Found");
                        }
                    } else {
                        sendResponse(exchange, 400, "Cannot delete primary card");
                    }
                } else {
                    sendResponse(exchange, 404, "Card Not Found");
                }
            }
        } catch (SQLException e) {
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    @Override
    protected void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
