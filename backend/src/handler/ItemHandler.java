package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.InvalidInputException;
import exception.ItemNotFoundException;
import exception.UnauthorizedException;
import model.*;
import service.AuthService;
import service.ItemService;
import util.HttpUtil;
import util.JsonUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

// HTTP Handler managing item lifecycle (CRUD, filters, smart matches)
public class ItemHandler implements HttpHandler {

    private final ItemService itemService;
    private final AuthService authService;

    public ItemHandler(ItemService itemService, AuthService authService) {
        this.itemService = itemService;
        this.authService = authService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpUtil.handlePreflight(exchange)) {
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("GET".equalsIgnoreCase(method)) {
                handleGet(exchange, path);
            } else if ("POST".equalsIgnoreCase(method)) {
                handlePost(exchange);
            } else if ("PUT".equalsIgnoreCase(method)) {
                handlePut(exchange, path);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                handleDelete(exchange, path);
            } else {
                HttpUtil.sendError(exchange, 405, "Method " + method + " not supported.");
            }
        } catch (InvalidInputException iie) {
            // OOP CONCEPT: Exception Handling (Map custom exception to HTTP 400)
            HttpUtil.sendError(exchange, 400, iie.getMessage());
        } catch (UnauthorizedException ue) {
            // OOP CONCEPT: Exception Handling (Map custom exception to HTTP 401)
            HttpUtil.sendError(exchange, 401, ue.getMessage());
        } catch (ItemNotFoundException infe) {
            // OOP CONCEPT: Exception Handling (Map custom exception to HTTP 404)
            HttpUtil.sendError(exchange, 404, infe.getMessage());
        } catch (Exception e) {
            // OOP CONCEPT: Exception Handling (Fallback generic 500)
            HttpUtil.sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException, ItemNotFoundException {
        // Path pattern: /api/items/{id}/matches
        if (path.matches("^/api/items/\\d+/matches/?$")) {
            String[] segments = path.split("/");
            int id = Integer.parseInt(segments[3]);
            ArrayList<String> matches = itemService.findMatchesJson(id);
            HttpUtil.sendJsonResponse(exchange, 200, JsonUtil.toJsonArray(matches));
            return;
        }

        // Path pattern: /api/items/{id}
        if (path.matches("^/api/items/\\d+/?$")) {
            String[] segments = path.split("/");
            int id = Integer.parseInt(segments[3]);
            Item item = itemService.getById(id);
            HttpUtil.sendJsonResponse(exchange, 200, item.toJson());
            return;
        }

        // Default: List / Filter items (/api/items)
        Map<String, String> qParams = HttpUtil.parseQueryParams(exchange);
        String type = qParams.get("type");
        String categoryStr = qParams.get("category");
        String statusStr = qParams.get("status");
        String query = qParams.get("q");

        Category category = null;
        if (categoryStr != null && !categoryStr.trim().isEmpty() && !categoryStr.equalsIgnoreCase("ALL")) {
            category = Category.fromString(categoryStr);
        }

        ItemStatus status = null;
        if (statusStr != null && !statusStr.trim().isEmpty() && !statusStr.equalsIgnoreCase("ALL")) {
            status = ItemStatus.fromString(statusStr);
        }

        ArrayList<Item> filtered = itemService.filter(type, category, status, query);
        ArrayList<String> jsonItems = new ArrayList<String>();
        for (int i = 0; i < filtered.size(); i++) {
            jsonItems.add(filtered.get(i).toJson());
        }

        HttpUtil.sendJsonResponse(exchange, 200, JsonUtil.toJsonArray(jsonItems));
    }

    private void handlePost(HttpExchange exchange) throws IOException, InvalidInputException {
        Map<String, String> params = HttpUtil.parseFormData(exchange);

        String type = params.get("type");
        String title = params.get("title");
        String description = params.get("description");
        String categoryStr = params.get("category");
        String location = params.get("location");
        String date = params.get("date");
        String reporterRole = params.get("reporterRole");
        String reporterId = params.get("reporterId");
        String reporterName = params.get("reporterName");
        String reporterContact = params.get("reporterContact");

        if (type == null || (!type.equalsIgnoreCase("LOST") && !type.equalsIgnoreCase("FOUND"))) {
            throw new InvalidInputException("Item type must be either LOST or FOUND.");
        }

        Category category = Category.fromString(categoryStr);

        // OOP CONCEPT: Polymorphism (Instantiating Student or Staff through User reference)
        User reporter;
        if ("STAFF".equalsIgnoreCase(reporterRole)) {
            reporter = new Staff(reporterId, reporterName, reporterContact);
        } else {
            reporter = new Student(reporterId, reporterName, reporterContact);
        }

        int newId = Item.generateNextId();
        Item item;
        // OOP CONCEPT: Polymorphism (Instantiating LostItem or FoundItem through Item reference)
        if ("LOST".equalsIgnoreCase(type)) {
            item = new LostItem(newId, title, description, category, location, date, ItemStatus.OPEN, reporter);
        } else {
            item = new FoundItem(newId, title, description, category, location, date, ItemStatus.OPEN, reporter);
        }

        itemService.add(item);
        HttpUtil.sendJsonResponse(exchange, 201, item.toJson());
    }

    private void handlePut(HttpExchange exchange, String path) 
            throws IOException, UnauthorizedException, ItemNotFoundException, InvalidInputException {
        // Enforce Admin token check
        String token = HttpUtil.getAdminToken(exchange);
        if (!authService.isValidToken(token)) {
            throw new UnauthorizedException("Admin authorization token is missing or invalid.");
        }

        String[] segments = path.split("/");
        if (segments.length < 4) {
            throw new InvalidInputException("Item ID is required in URL path.");
        }
        int id = Integer.parseInt(segments[3]);

        Item existing = itemService.getById(id);
        Map<String, String> params = HttpUtil.parseFormData(exchange);

        if (params.containsKey("status")) {
            existing.setStatus(ItemStatus.fromString(params.get("status")));
        }
        if (params.containsKey("title") && !params.get("title").trim().isEmpty()) {
            existing.setTitle(params.get("title"));
        }
        if (params.containsKey("description") && !params.get("description").trim().isEmpty()) {
            existing.setDescription(params.get("description"));
        }
        if (params.containsKey("location") && !params.get("location").trim().isEmpty()) {
            existing.setLocation(params.get("location"));
        }
        if (params.containsKey("category") && !params.get("category").trim().isEmpty()) {
            existing.setCategory(Category.fromString(params.get("category")));
        }

        itemService.update(existing);
        HttpUtil.sendJsonResponse(exchange, 200, existing.toJson());
    }

    private void handleDelete(HttpExchange exchange, String path) 
            throws IOException, UnauthorizedException, ItemNotFoundException, InvalidInputException {
        // Enforce Admin token check
        String token = HttpUtil.getAdminToken(exchange);
        if (!authService.isValidToken(token)) {
            throw new UnauthorizedException("Admin authorization token is missing or invalid.");
        }

        String[] segments = path.split("/");
        if (segments.length < 4) {
            throw new InvalidInputException("Item ID is required in URL path.");
        }
        int id = Integer.parseInt(segments[3]);

        itemService.delete(id);
        HttpUtil.sendJsonResponse(exchange, 200, "{\"message\":\"Item #" + id + " deleted successfully\"}");
    }
}
