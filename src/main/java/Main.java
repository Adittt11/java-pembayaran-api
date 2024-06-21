import com.sun.net.httpserver.HttpServer;
import handlers.CardHandler;
import handlers.CustomerHandler;
import handlers.ItemHandler;
import handlers.SubscriptionHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    private static final int PORT = 9128;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/customers", new CustomerHandler());
        server.createContext("/subscriptions", new SubscriptionHandler());
        server.createContext("/items", new ItemHandler());
        server.createContext("/cards", new CardHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("Server started on port " + PORT);
    }
}
