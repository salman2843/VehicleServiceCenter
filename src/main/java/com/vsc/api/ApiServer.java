package com.vsc.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.vsc.exception.InsufficientStockException;
import com.vsc.exception.InvalidStatusTransitionException;
import com.vsc.exception.ValidationException;
import com.vsc.util.AppInfo;
import com.vsc.util.DBConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The HTTP bridge between the React frontend and the Java service layer.
 *
 * <p>Uses only {@code com.sun.net.httpserver} from the JDK, so the Maven build
 * still has one runtime dependency (the MySQL driver). Every request is handled
 * by the existing services - the API contains no SQL and no business rules.</p>
 *
 * <p>Run with {@code mvn exec:java}; the React dev server proxies to this port.</p>
 */
public final class ApiServer implements AutoCloseable {

    public static final int DEFAULT_PORT = 8080;

    private final HttpServer server;
    private final ExecutorService workers;
    private final int port;

    public ApiServer(int port) throws IOException {
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.workers = Executors.newFixedThreadPool(8, runnable -> {
            Thread thread = new Thread(runnable, "vsc-api");
            thread.setDaemon(true);
            return thread;
        });
        server.setExecutor(workers);

        ApiContext context = new ApiContext();
        Router router = new Router();
        router.get("/api/health", request -> Json.obj(
                "status", "ok",
                "app", AppInfo.NAME,
                "version", AppInfo.VERSION,
                "stage", AppInfo.BUILD_STAGE,
                "database", DBConnection.describeTarget()));
        DashboardApi.register(router, context);
        CustomerApi.register(router, context);
        VehicleApi.register(router, context);
        MechanicApi.register(router, context);
        CatalogApi.register(router, context);
        InventoryApi.register(router, context);
        JobApi.register(router, context);
        InvoiceApi.register(router, context);

        server.createContext("/api", exchange -> handleApi(exchange, router));
        server.createContext("/", exchange -> StaticFiles.serve(exchange));
    }

    public void start() {
        server.start();
    }

    public int getPort() {
        return port;
    }

    public String url() {
        return "http://localhost:" + port;
    }

    @Override
    public void close() {
        server.stop(0);
        workers.shutdownNow();
    }

    private static void handleApi(HttpExchange exchange, Router router) throws IOException {
        addCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        int status = 200;
        Object payload;
        try {
            String body = readBody(exchange);
            Map<String, Object> json = body.isBlank() ? new LinkedHashMap<>() : Json.parseObject(body);
            Object result = router.dispatch(exchange.getRequestMethod(),
                    exchange.getRequestURI().getPath(), queryOf(exchange), json);
            if (result instanceof ApiResponse response) {
                status = response.status();
                payload = response.body();
            } else {
                payload = result;
            }
        } catch (ApiException e) {
            status = e.getStatus();
            payload = error(e.getMessage(), e.getField());
        } catch (ValidationException e) {
            status = 400;
            payload = error(e.getMessage(), e.getField());
        } catch (InsufficientStockException e) {
            status = 409;
            payload = Json.obj("error", e.getMessage(), "field", "quantity",
                    "partName", e.getPartName(), "requested", e.getRequested(),
                    "available", e.getAvailable());
        } catch (InvalidStatusTransitionException e) {
            status = 409;
            payload = Json.obj("error", e.getMessage(), "field", "status",
                    "current", e.getCurrent(),
                    "requested", e.getRequested());
        } catch (SQLException e) {
            status = 503;
            payload = error(e.getMessage(), null);
        } catch (Exception e) {
            status = 500;
            payload = error("Unexpected server error: " + e.getMessage(), null);
            e.printStackTrace();
        }
        send(exchange, status, payload);
    }

    private static Map<String, Object> error(String message, String field) {
        return Json.obj("error", message == null ? "Request failed." : message, "field", field);
    }

    private static void addCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "86400");
    }

    private static void send(HttpExchange exchange, int status, Object payload) throws IOException {
        if (status == 204 || payload == null) {
            exchange.sendResponseHeaders(status == 200 ? 204 : status, -1);
            exchange.close();
            return;
        }
        byte[] bytes = Json.write(payload).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static Map<String, String> queryOf(HttpExchange exchange) {
        Map<String, String> query = new LinkedHashMap<>();
        String raw = exchange.getRequestURI().getRawQuery();
        if (raw == null || raw.isBlank()) {
            return query;
        }
        for (String pair : raw.split("&")) {
            int equals = pair.indexOf('=');
            String key = equals < 0 ? pair : pair.substring(0, equals);
            String value = equals < 0 ? "" : pair.substring(equals + 1);
            query.put(URLDecoder.decode(key, StandardCharsets.UTF_8),
                    URLDecoder.decode(value, StandardCharsets.UTF_8));
        }
        return query;
    }
}
