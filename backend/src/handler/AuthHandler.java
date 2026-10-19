package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.UnauthorizedException;
import service.AuthService;
import util.HttpUtil;
import util.JsonUtil;

import java.io.IOException;
import java.util.Map;

// HTTP Handler managing administrative login and session termination
public class AuthHandler implements HttpHandler {

    private final AuthService authService;

    public AuthHandler(AuthService authService) {
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
            if ("POST".equalsIgnoreCase(method)) {
                if (path.endsWith("/logout")) {
                    handleLogout(exchange);
                } else {
                    handleLogin(exchange);
                }
            } else {
                HttpUtil.sendError(exchange, 405, "Method " + method + " not allowed.");
            }
        } catch (UnauthorizedException ue) {
            // OOP CONCEPT: Exception Handling (Catching custom exception and mapping to 401 Unauthorized)
            HttpUtil.sendError(exchange, 401, ue.getMessage());
        } catch (Exception e) {
            HttpUtil.sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException, UnauthorizedException {
        Map<String, String> params = HttpUtil.parseFormData(exchange);
        String username = params.get("username");
        String password = params.get("password");

        String token = authService.login(username, password);

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"token\":").append(JsonUtil.toJsonString(token)).append(",");
        sb.append("\"username\":").append(JsonUtil.toJsonString(username)).append(",");
        sb.append("\"role\":\"ADMIN\",");
        sb.append("\"message\":\"Login successful\"");
        sb.append("}");

        HttpUtil.sendJsonResponse(exchange, 200, sb.toString());
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        String token = HttpUtil.getAdminToken(exchange);
        if (token == null) {
            Map<String, String> params = HttpUtil.parseFormData(exchange);
            token = params.get("token");
        }

        if (token != null) {
            authService.logout(token);
        }

        HttpUtil.sendJsonResponse(exchange, 200, "{\"message\":\"Logged out successfully\"}");
    }
}
