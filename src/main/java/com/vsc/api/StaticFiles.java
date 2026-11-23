package com.vsc.api;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Serves the built React app from {@code frontend/dist}.
 *
 * <p>In development the UI runs on Vite's port and proxies {@code /api} here, so
 * this class only matters for the "single command" mode: {@code mvn exec:java}
 * after {@code npm run build}. When the folder is missing it explains how to
 * build instead of showing a blank page.</p>
 */
final class StaticFiles {

    private static final Path ROOT = Path.of(
            System.getProperty("vsc.web.root", "frontend/dist")).toAbsolutePath().normalize();

    private StaticFiles() {
        // utility class, no instances
    }

    static void serve(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        String path = exchange.getRequestURI().getPath();
        Path file = resolve(path);
        if (file == null) {
            file = resolve("/index.html");
        }
        if (file == null) {
            sendBuildHint(exchange);
            return;
        }

        byte[] bytes = Files.readAllBytes(file);
        exchange.getResponseHeaders().set("Content-Type", contentType(file.getFileName().toString()));
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    /** Null when the path is outside the web root or is not a file. */
    private static Path resolve(String requestPath) {
        if (requestPath == null || requestPath.isEmpty() || requestPath.equals("/")) {
            requestPath = "/index.html";
        }
        Path candidate = ROOT.resolve(requestPath.substring(1)).normalize();
        if (!candidate.startsWith(ROOT) || !Files.isRegularFile(candidate)) {
            return null;
        }
        return candidate;
    }

    private static String contentType(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (lower.endsWith(".js")) {
            return "text/javascript; charset=utf-8";
        }
        if (lower.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (lower.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".ico")) {
            return "image/x-icon";
        }
        if (lower.endsWith(".json")) {
            return "application/json; charset=utf-8";
        }
        if (lower.endsWith(".woff2")) {
            return "font/woff2";
        }
        return "application/octet-stream";
    }

    private static void sendBuildHint(HttpExchange exchange) throws IOException {
        String html = """
                <!doctype html>
                <html lang="en">
                  <head>
                    <meta charset="utf-8" />
                    <title>Vehicle Service Center - API running</title>
                    <style>
                      body { font-family: Georgia, serif; background: #f4f1ea; color: #14110f;
                             margin: 0; display: grid; place-items: center; min-height: 100vh; }
                      main { max-width: 46rem; padding: 3rem; border: 1px solid #ded7c9; background: #fbf9f5; }
                      code { font-family: Consolas, monospace; background: #ece7db; padding: .15rem .35rem; }
                      h1 { font-size: 2rem; margin-top: 0; }
                    </style>
                  </head>
                  <body>
                    <main>
                      <h1>The API is running.</h1>
                      <p>The React build was not found in <code>frontend/dist</code>.</p>
                      <p>Start the development UI:</p>
                      <p><code>cd frontend</code><br /><code>npm install</code><br /><code>npm run dev</code></p>
                      <p>Or build it once and reload this page:</p>
                      <p><code>npm run build</code></p>
                    </main>
                  </body>
                </html>
                """;
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
