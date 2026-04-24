import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;

public class WebServer {
    private static final int DEFAULT_PORT = 8080;
    private static final Path WEB_ROOT = Path.of("web");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ComplaintManager manager = new ComplaintManager();

    public static void main(String[] args) throws Exception {
        new WebServer().start();
    }

    private void start() throws Exception {
        int port = readPort();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/complaints", new ComplaintApiHandler());
        server.createContext("/", new StaticFileHandler());
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        System.out.println("Customer Complaint Portal is running at http://localhost:" + port);
        System.out.println("Storage: " + manager.getStorageName());
    }

    private int readPort() {
        String value = System.getenv("PORT");
        if (value == null || value.isBlank()) {
            return DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return DEFAULT_PORT;
        }
    }

    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendText(exchange, 405, "Method Not Allowed");
                return;
            }

            String requestPath = exchange.getRequestURI().getPath();
            String cleanPath = requestPath.equals("/") ? "index.html" : requestPath.substring(1);
            Path filePath = WEB_ROOT.resolve(cleanPath).normalize();

            if (!filePath.startsWith(WEB_ROOT) || Files.notExists(filePath) || Files.isDirectory(filePath)) {
                filePath = WEB_ROOT.resolve("index.html");
            }

            byte[] content = Files.readAllBytes(filePath);
            Headers headers = exchange.getResponseHeaders();
            String contentType = URLConnection.guessContentTypeFromName(filePath.getFileName().toString());
            headers.set("Content-Type", contentType == null ? "text/html; charset=utf-8" : contentType + (contentType.startsWith("text/") ? "; charset=utf-8" : ""));
            exchange.sendResponseHeaders(200, content.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(content);
            }
        }
    }

    private class ComplaintApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();
                String suffix = path.length() > "/api/complaints".length() ? path.substring("/api/complaints".length()) : "";

                if ("GET".equalsIgnoreCase(method) && (suffix.isEmpty() || "/".equals(suffix))) {
                    sendJson(exchange, 200, buildListResponse(manager.viewComplaints(), manager.getStorageName()));
                    return;
                }

                if ("POST".equalsIgnoreCase(method) && (suffix.isEmpty() || "/".equals(suffix))) {
                    handleCreate(exchange);
                    return;
                }

                if (suffix.startsWith("/")) {
                    String trimmed = suffix.substring(1);
                    String[] parts = trimmed.split("/");
                    int id = Integer.parseInt(parts[0]);

                    if ("GET".equalsIgnoreCase(method) && parts.length == 1) {
                        Optional<Complaint> complaint = manager.searchComplaint(id);
                        if (complaint.isPresent()) {
                            sendJson(exchange, 200, buildComplaintResponse(complaint.get(), manager.getStorageName()));
                        } else {
                            sendJson(exchange, 404, "{\"message\":\"Complaint not found.\"}");
                        }
                        return;
                    }

                    if ("PUT".equalsIgnoreCase(method) && parts.length == 2 && "status".equals(parts[1])) {
                        handleStatusUpdate(exchange, id);
                        return;
                    }

                    if ("DELETE".equalsIgnoreCase(method) && parts.length == 1) {
                        boolean deleted = manager.deleteComplaint(id);
                        if (deleted) {
                            sendJson(exchange, 200, "{\"message\":\"Complaint deleted successfully.\"}");
                        } else {
                            sendJson(exchange, 404, "{\"message\":\"Complaint not found.\"}");
                        }
                        return;
                    }
                }

                sendJson(exchange, 404, "{\"message\":\"Endpoint not found.\"}");
            } catch (NumberFormatException exception) {
                sendJson(exchange, 400, "{\"message\":\"Invalid complaint ID.\"}");
            } catch (Exception exception) {
                sendJson(exchange, 500, "{\"message\":" + quote(exception.getMessage()) + "}");
            }
        }

        private void handleCreate(HttpExchange exchange) throws Exception {
            String body = readBody(exchange);
            Integer id = extractInt(body, "id");
            String description = extractString(body, "description");

            if (id == null || id <= 0) {
                sendJson(exchange, 400, "{\"message\":\"Complaint ID must be a positive number.\"}");
                return;
            }
            if (description == null || description.isBlank()) {
                sendJson(exchange, 400, "{\"message\":\"Complaint description cannot be empty.\"}");
                return;
            }

            Complaint complaint = new Complaint(id, description.trim());
            manager.addComplaint(complaint);
            sendJson(exchange, 201, buildComplaintResponse(complaint, manager.getStorageName()));
        }

        private void handleStatusUpdate(HttpExchange exchange, int id) throws Exception {
            String body = readBody(exchange);
            String status = extractString(body, "status");

            if (status == null || status.isBlank()) {
                sendJson(exchange, 400, "{\"message\":\"Status cannot be empty.\"}");
                return;
            }

            boolean updated = manager.updateStatus(id, status.trim());
            if (!updated) {
                sendJson(exchange, 404, "{\"message\":\"Complaint not found.\"}");
                return;
            }

            Optional<Complaint> updatedComplaint = manager.searchComplaint(id);
            if (updatedComplaint.isPresent()) {
                sendJson(exchange, 200, buildComplaintResponse(updatedComplaint.get(), manager.getStorageName()));
            } else {
                sendJson(exchange, 200, "{\"message\":\"Status updated successfully.\"}");
            }
        }
    }

    private void sendText(HttpExchange exchange, int status, String text) throws IOException {
        byte[] content = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, content.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(content);
        }
    }

    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] content = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, content.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(content);
        }
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String buildListResponse(List<Complaint> complaints, String storageName) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"storage\":").append(quote(storageName)).append(",\"complaints\":[");
        for (int i = 0; i < complaints.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(toJson(complaints.get(i)));
        }
        builder.append("]}");
        return builder.toString();
    }

    private String buildComplaintResponse(Complaint complaint, String storageName) {
        return "{\"storage\":" + quote(storageName) + ",\"complaint\":" + toJson(complaint) + "}";
    }

    private String toJson(Complaint complaint) {
        return "{"
                + "\"id\":" + complaint.getId() + ','
                + "\"description\":" + quote(complaint.getDescription()) + ','
                + "\"priority\":" + complaint.getPriority() + ','
                + "\"priorityLevel\":" + quote(complaint.getPriorityLevel()) + ','
                + "\"department\":" + quote(complaint.getDepartment()) + ','
                + "\"status\":" + quote(complaint.getStatus()) + ','
                + "\"highFlag\":" + (complaint.isHighPriority() ? 1 : 0) + ','
                + "\"createdAt\":" + quote(complaint.getCreatedAt().format(DATE_FORMATTER))
                + "}";
    }

    private Integer extractInt(String body, String key) {
        String value = extractRawValue(body, key);
        return value == null ? null : Integer.parseInt(value.trim());
    }

    private String extractString(String body, String key) {
        String marker = "\"" + key + "\"";
        int start = body.indexOf(marker);
        if (start < 0) {
            return null;
        }
        int colon = body.indexOf(':', start);
        int firstQuote = body.indexOf('"', colon + 1);
        int secondQuote = body.indexOf('"', firstQuote + 1);
        if (colon < 0 || firstQuote < 0 || secondQuote < 0) {
            return null;
        }
        return body.substring(firstQuote + 1, secondQuote)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private String extractRawValue(String body, String key) {
        String marker = "\"" + key + "\"";
        int start = body.indexOf(marker);
        if (start < 0) {
            return null;
        }
        int colon = body.indexOf(':', start);
        int end = body.indexOf(',', colon + 1);
        if (end < 0) {
            end = body.indexOf('}', colon + 1);
        }
        if (colon < 0 || end < 0) {
            return null;
        }
        return body.substring(colon + 1, end).trim();
    }

    private String quote(String value) {
        if (value == null) {
            return "null";
        }
        return "\""
                + value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "\\n")
                + "\"";
    }
}
