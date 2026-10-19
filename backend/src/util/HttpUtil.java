package util;

import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

// Utility class for reading HTTP request data and writing JSON responses
public class HttpUtil {

    // Reads and parses URL-encoded form data (application/x-www-form-urlencoded)
    public static Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        String body = sb.toString();
        if (body.isEmpty()) {
            return params;
        }

        String[] pairs = body.split("&");
        for (int i = 0; i < pairs.length; i++) {
            String pair = pairs[i];
            int idx = pair.indexOf("=");
            if (idx > 0 && idx < pair.length() - 1) {
                String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                params.put(key, value);
            } else if (idx > 0 && idx == pair.length() - 1) {
                String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                params.put(key, "");
            }
        }
        return params;
    }

    // Parses query parameters from request URL (e.g., ?type=LOST&category=ELECTRONICS)
    public static Map<String, String> parseQueryParams(HttpExchange exchange) {
        Map<String, String> params = new HashMap<String, String>();
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null || query.trim().isEmpty()) {
            return params;
        }

        String[] pairs = query.split("&");
        for (int i = 0; i < pairs.length; i++) {
            String pair = pairs[i];
            int idx = pair.indexOf("=");
            try {
                if (idx > 0 && idx < pair.length() - 1) {
                    String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                    params.put(key, value);
                } else if (idx > 0 && idx == pair.length() - 1) {
                    String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    params.put(key, "");
                }
            } catch (UnsupportedEncodingException ignored) {}
        }
        return params;
    }

    // Sends a JSON response with status code and headers
    public static void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, X-Admin-Token");

        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    // Sends standardized JSON error response
    public static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        String json = "{\"error\":" + JsonUtil.toJsonString(message) + "}";
        sendJsonResponse(exchange, statusCode, json);
    }

    // Handles CORS preflight OPTIONS requests
    public static boolean handlePreflight(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, X-Admin-Token");
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    // Helper to extract X-Admin-Token header
    public static String getAdminToken(HttpExchange exchange) {
        return exchange.getRequestHeaders().getFirst("X-Admin-Token");
    }
}
