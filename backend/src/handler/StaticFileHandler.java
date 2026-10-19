package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;

// Serves the frontend static files (HTML, CSS, JS) so the entire system runs seamlessly
public class StaticFileHandler implements HttpHandler {

    private final String frontendDirPath;

    public StaticFileHandler(String frontendDirPath) {
        this.frontendDirPath = frontendDirPath;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        URI uri = exchange.getRequestURI();
        String path = uri.getPath();

        // Default to index.html for root path
        if (path == null || path.equals("/") || path.isEmpty()) {
            path = "/index.html";
        }

        // Security check: prevent directory traversal attacks
        if (path.contains("..") || path.contains("./")) {
            byte[] errorBytes = "403 Forbidden: Directory traversal blocked".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(403, errorBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(errorBytes);
            os.close();
            return;
        }

        // Resolve absolute target file path
        File baseDir = new File(frontendDirPath).getCanonicalFile();
        File requestedFile = new File(baseDir, path).getCanonicalFile();

        // Ensure requested file is strictly inside frontend directory
        if (!requestedFile.getPath().startsWith(baseDir.getPath()) || !requestedFile.exists() || requestedFile.isDirectory()) {
            byte[] notFoundBytes = "404 Not Found: Static resource not found".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(404, notFoundBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(notFoundBytes);
            os.close();
            return;
        }

        // Determine MIME Content-Type
        String contentType = "text/plain";
        String lowerPath = requestedFile.getName().toLowerCase();
        if (lowerPath.endsWith(".html") || lowerPath.endsWith(".htm")) {
            contentType = "text/html; charset=UTF-8";
        } else if (lowerPath.endsWith(".css")) {
            contentType = "text/css; charset=UTF-8";
        } else if (lowerPath.endsWith(".js")) {
            contentType = "application/javascript; charset=UTF-8";
        } else if (lowerPath.endsWith(".json")) {
            contentType = "application/json; charset=UTF-8";
        } else if (lowerPath.endsWith(".ico")) {
            contentType = "image/x-icon";
        } else if (lowerPath.endsWith(".svg")) {
            contentType = "image/svg+xml";
        } else if (lowerPath.endsWith(".png")) {
            contentType = "image/png";
        }

        byte[] fileBytes = new byte[(int) requestedFile.length()];
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(requestedFile);
            int offset = 0;
            int numRead;
            while (offset < fileBytes.length && (numRead = fis.read(fileBytes, offset, fileBytes.length - offset)) >= 0) {
                offset += numRead;
            }
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ignored) {}
            }
        }

        exchange.getResponseHeaders().set("Content-Type", contentType);
        if ("HEAD".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(200, -1);
        } else {
            exchange.sendResponseHeaders(200, fileBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(fileBytes);
            os.close();
        }
    }
}
