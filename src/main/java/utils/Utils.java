package utils;

import java.sql.Connection;
import java.sql.Statement;

public class Utils {
    public static void createTables(Connection connection) {
        String createCustomersTable = "CREATE TABLE IF NOT EXISTS customers (" +
                "id INTEGER PRIMARY KEY, " +
                "email TEXT NOT NULL, " +
                "first_name TEXT NOT NULL, " +
                "last_name TEXT NOT NULL, " +
                "phone_number TEXT NOT NULL);";

        String createShippingAddressesTable = "CREATE TABLE IF NOT EXISTS shipping_addresses (" +
                "id INTEGER PRIMARY KEY, " +
                "customer INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "line1 TEXT NOT NULL, " +
                "line2 TEXT, " +
                "city TEXT NOT NULL, " +
                "province TEXT NOT NULL, " +
                "postocde TEXT NOT NULL);";

        String createSubscriptionsTable = "CREATE TABLE IF NOT EXISTS subscriptions (" +
                "id INTEGER PRIMARY KEY, " +
                "customer INTEGER NOT NULL, " +
                "billing_period INTEGER NOT NULL, " +
                "billing_period_unit TEXT NOT NULL, " +
                "total_due INTEGER NOT NULL, " +
                "activated_at TEXT NOT NULL, " +
                "current_term_start TEXT NOT NULL, " +
                "current_term_end TEXT NOT NULL, " +
                "status TEXT NOT NULL);";

        String createSubscriptionItemsTable = "CREATE TABLE IF NOT EXISTS subscription_items (" +
                "subscription INTEGER, " +
                "item INTEGER, " +
                "quantity INTEGER NOT NULL, " +
                "price INTEGER NOT NULL, " +
                "amount INTEGER NOT NULL, " +
                "PRIMARY KEY (subscription, item));";

        String createItemsTable = "CREATE TABLE IF NOT EXISTS items (" +
                "id INTEGER PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "price INTEGER NOT NULL, " +
                "type TEXT NOT NULL, " +
                "is_active INTEGER NOT NULL);";

        String createCardsTable = "CREATE TABLE IF NOT EXISTS cards (" +
                "id INTEGER PRIMARY KEY, " +
                "customer INTEGER NOT NULL, " +
                "card_type TEXT NOT NULL, " +
                "masked_number TEXT NOT NULL, " +
                "expiry_month INTEGER NOT NULL, " +
                "expiry_year INTEGER NOT NULL, " +
                "status TEXT NOT NULL, " +
                "is_primary INTEGER NOT NULL);";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createCustomersTable);
            stmt.execute(createShippingAddressesTable);
            stmt.execute(createSubscriptionsTable);
            stmt.execute(createSubscriptionItemsTable);
            stmt.execute(createItemsTable);
            stmt.execute(createCardsTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
