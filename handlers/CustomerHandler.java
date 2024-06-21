package handlers;

import com.google.gson.Gson;
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

public class CustomerHandler extends BaseHandler {

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
                // GET /customers
                String query = "SELECT * FROM customers";
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();
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
                sendResponse(exchange, 200, customers.toString());
            } else if (segments.length == 3) {
                int customerId = Integer.parseInt(segments[2]);
                // GET /customers/{id}
                String query = "SELECT * FROM customers WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, customerId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    JSONObject customer = new JSONObject();
                    customer.put("id", rs.getInt("id"));
                    customer.put("email", rs.getString("email"));
                    customer.put("first_name", rs.getString("first_name"));
                    customer.put("last_name", rs.getString("last_name"));
                    customer.put("phone_number", rs.getString("phone_number"));

                    // Get addresses
                    String addressQuery = "SELECT * FROM shipping_addresses WHERE customer = ?";
                    PreparedStatement addrStmt = conn.prepareStatement(addressQuery);
                    addrStmt.setInt(1, customerId);
                    ResultSet addrRs = addrStmt.executeQuery();
                    JSONArray addresses = new JSONArray();
                    while (addrRs.next()) {
                        JSONObject address = new JSONObject();
                        address.put("id", addrRs.getInt("id"));
                        address.put("title", addrRs.getString("title"));
                        address.put("line1", addrRs.getString("line1"));
                        address.put("line2", addrRs.getString("line2"));
                        address.put("city", addrRs.getString("city"));
                        address.put("province", addrRs.getString("province"));
                        address.put("postcode", addrRs.getString("postcode"));
                        addresses.put(address);
                    }
                    customer.put("addresses", addresses);
                    sendResponse(exchange, 200, customer.toString());
                } else {
                    sendResponse(exchange, 404, "Customer not found");
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

        JSONObject customerJson = new JSONObject(requestBody.toString());

        try (Connection conn = connect()) {
            String query = "INSERT INTO customers (email, first_name, last_name, phone_number) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, customerJson.getString("email"));
            stmt.setString(2, customerJson.getString("first_name"));
            stmt.setString(3, customerJson.getString("last_name"));
            stmt.setString(4, customerJson.getString("phone_number"));
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                sendResponse(exchange, 201, "Customer created successfully");
            } else {
                sendResponse(exchange, 400, "Failed to create customer");
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

        int customerId = Integer.parseInt(segments[2]);

        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }

        JSONObject customerJson = new JSONObject(requestBody.toString());

        try (Connection conn = connect()) {
            String query = "UPDATE customers SET email = ?, first_name = ?, last_name = ?, phone_number = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, customerJson.getString("email"));
            stmt.setString(2, customerJson.getString("first_name"));
            stmt.setString(3, customerJson.getString("last_name"));
            stmt.setString(4, customerJson.getString("phone_number"));
            stmt.setInt(5, customerId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                sendResponse(exchange, 200, "Customer updated successfully");
            } else {
                sendResponse(exchange, 404, "Customer not found");
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

        int customerId = Integer.parseInt(segments[2]);

        try (Connection conn = connect()) {
            String query = "DELETE FROM customers WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, customerId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                sendResponse(exchange, 200, "Customer deleted successfully");
            } else {
                sendResponse(exchange, 404, "Customer not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error");

            //work with json library
            Gson gson = new Gson();

        }
    }
}

//

