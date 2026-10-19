import com.sun.net.httpserver.HttpServer;
import handler.*;
import service.AuthService;
import service.ClaimService;
import service.ItemService;
import util.FileStorage;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

// Application entry point for Smart Campus Lost & Found Management System
public class Main {

    // OOP CONCEPT: static and final constants
    public static final int SERVER_PORT = 8080;
    public static final String DATA_DIR = "data";
    public static final String ITEMS_FILE = DATA_DIR + File.separator + "items.txt";
    public static final String CLAIMS_FILE = DATA_DIR + File.separator + "claims.txt";
    public static final String FRONTEND_DIR = "frontend";

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("     SMART CAMPUS LOST & FOUND MANAGEMENT SYSTEM (v1.0)");
        System.out.println("==================================================================");

        try {
            // 1. Initialize persistent storage (data files and sample records)
            FileStorage fileStorage = new FileStorage(ITEMS_FILE, CLAIMS_FILE);

            // 2. Initialize core services
            // OOP CONCEPT: Abstraction & Composition (Services orchestrate business logic)
            ItemService itemService = new ItemService(fileStorage);
            ClaimService claimService = new ClaimService(fileStorage);
            AuthService authService = new AuthService();

            // 3. Initialize HTTP Handlers
            ItemHandler itemHandler = new ItemHandler(itemService, authService);
            ClaimHandler claimHandler = new ClaimHandler(claimService, itemService, authService);
            AuthHandler authHandler = new AuthHandler(authService);
            StatsHandler statsHandler = new StatsHandler(itemService, claimService);
            StaticFileHandler staticFileHandler = new StaticFileHandler(FRONTEND_DIR);

            // 4. Create and configure built-in Java HTTP Server
            HttpServer server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);

            // Register API endpoints
            server.createContext("/api/items", itemHandler);
            server.createContext("/api/claims", claimHandler);
            server.createContext("/api/login", authHandler);
            server.createContext("/api/logout", authHandler);
            server.createContext("/api/stats", statsHandler);

            // Register static frontend handler for root and assets
            server.createContext("/", staticFileHandler);

            // Set executor to null (default system executor, no complex multithreading)
            server.setExecutor(null);

            // 5. Start the server
            server.start();

            System.out.println(">>> Server successfully started on port " + SERVER_PORT);
            System.out.println(">>> Open in browser: http://localhost:" + SERVER_PORT);
            System.out.println(">>> Admin Credentials: Username: 'admin' | Password: 'password123'");
            System.out.println("==================================================================");

        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
