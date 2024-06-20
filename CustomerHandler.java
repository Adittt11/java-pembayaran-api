import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class CustomerHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!Main.isAuthenticated(exchange.getRequestHeaders().getFirst("API-Key"))) {
            Utils.sendResponse(exchange, "Forbidden", 403);
            return;
        }

        String method = exchange.getRequestMethod();
        String response = "";
        int responseCode = 200;

        try {
            switch (method) {
                case "GET":
                    String[] uriParts = exchange.getRequestURI().toString().split("/");
                    if (uriParts.length == 2) {
                        response = getAllCustomers();
                    } else if (uriParts.length == 3) {
                        int id = Integer.parseInt(uriParts[2]);
                        response = getCustomerById(id);
                    } else if (uriParts.length == 4 && uriParts[3].equals("cards")) {
                        int id = Integer.parseInt(uriParts[2]);
                        response = getCustomerCards(id);
                    } else if (uriParts.length == 4 && uriParts[3].equals("subscriptions")) {
                        int id = Integer.parseInt(uriParts[2]);
                        response = getCustomerSubscriptions(id, null);
                    } else if (uriParts.length == 5 && uriParts[3].equals("subscriptions")) {
                        int id = Integer.parseInt(uriParts[2]);
                        String status = uriParts[4];
                        response = getCustomerSubscriptions(id, status);
                    }
                    break;
                case "POST":
                    String requestBody = Utils.getRequestBody(exchange);
                    response = createCustomer(requestBody);
                    responseCode = 201;
                    break;
                case "PUT":
                    // Handle PUT request
                    break;
                case "DELETE":
                    // Handle DELETE request
                    break;
                default:
                    responseCode = 405;
                    response = "Method Not Allowed";
            }
        } catch (Exception e) {
            responseCode = 500;
            response = "Internal Server Error";
            e.printStackTrace();
        }

        Utils.sendResponse(exchange, response, responseCode);
    }

    private String getAllCustomers() {
        List<String> customers = new ArrayList<>();
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM customers")) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String customer = String.format(
                        "{\"id\": %d, \"email\": \"%s\", \"first_name\": \"%s\", \"last_name\": \"%s\", \"phone_number\": \"%s\"}",
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("phone_number"));
                customers.add(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "[" + String.join(",", customers) + "]";
    }

    private String getCustomerById(int id) {
        String response = "";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM customers WHERE id = ?")) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                response = String.format(
                        "{\"id\": %d, \"email\": \"%s\", \"first_name\": \"%s\", \"last_name\": \"%s\", \"phone_number\": \"%s\"}",
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("phone_number"));
            } else {
                response = "404 - Not Found";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response = "404 - Not Found";
        }
        return response;
    }

    private String getCustomerCards(int customerId) {
        List<String> cards = new ArrayList<>();
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM cards WHERE customer = ?")) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String card = String.format(
                        "{\"id\": %d, \"customer\": %d, \"card_type\": \"%s\", \"masked_number\": \"%s\", \"expiry_month\": %d, \"expiry_year\": %d, \"status\": \"%s\", \"is_primary\": %d}",
                        rs.getInt("id"),
                        rs.getInt("customer"),
                        rs.getString("card_type"),
                        rs.getString("masked_number"),
                        rs.getInt("expiry_month"),
                        rs.getInt("expiry_year"),
                        rs.getString("status"),
                        rs.getInt("is_primary"));
                cards.add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "[" + String.join(",", cards) + "]";
    }

    private String getCustomerSubscriptions(int customerId, String status) {
        List<String> subscriptions = new ArrayList<>();
        String query = "SELECT * FROM subscriptions WHERE customer = ?";
        if (status != null) {
            query += " AND status = ?";
        }

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            if (status != null) {
                pstmt.setString(2, status);
            }
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String subscription = String.format(
                        "{\"id\": %d, \"customer\": %d, \"billing_period\": %d, \"billing_period_unit\": \"%s\", \"total_due\": %d, \"activated_at\": \"%s\", \"current_term_start\": \"%s\", \"current_term_end\": \"%s\", \"status\": \"%s\"}",
                        rs.getInt("id"),
                        rs.getInt("customer"),
                        rs.getInt("billing_period"),
                        rs.getString("billing_period_unit"),
                        rs.getInt("total_due"),
                        rs.getString("activated_at"),
                        rs.getString("current_term_start"),
                        rs.getString("current_term_end"),
                        rs.getString("status"));
                subscriptions.add(subscription);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "[" + String.join(",", subscriptions) + "]";
    }

    private String createCustomer(String requestBody) {
        JSONObject json = Utils.parseJsonBody(requestBody);
        if (!json.has("email") || !json.has("first_name") || !json.has("last_name") || !json.has("phone_number")) {
            return "400 - Bad Request: Missing required fields";
        }

        String email = json.getString("email");
        String firstName = json.getString("first_name");
        String lastName = json.getString("last_name");
        String phoneNumber = json.getString("phone_number");

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO customers (email, first_name, last_name, phone_number) VALUES (?, ?, ?, ?)")) {
            pstmt.setString(1, email);
            pstmt.setString(2, firstName);
            pstmt.setString(3, lastName);
            pstmt.setString(4, phoneNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return "500 - Internal Server Error";
        }

        return "201 - Created";
    }
}
