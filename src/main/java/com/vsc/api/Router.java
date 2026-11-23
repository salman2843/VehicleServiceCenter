package com.vsc.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tiny path router: exact segments match, {@code {name}} captures a value.
 * Keeping it here avoids pulling a web framework into a project that is meant
 * to stay plain Java (AGENTS.md, tech stack).
 */
public final class Router {

    /** One endpoint implementation. */
    @FunctionalInterface
    public interface Handler {
        Object handle(ApiRequest request) throws Exception;
    }

    private record Route(String method, String[] pattern, Handler handler) {
    }

    private final List<Route> routes = new ArrayList<>();

    public Router get(String path, Handler handler) {
        return add("GET", path, handler);
    }

    public Router post(String path, Handler handler) {
        return add("POST", path, handler);
    }

    public Router put(String path, Handler handler) {
        return add("PUT", path, handler);
    }

    public Router delete(String path, Handler handler) {
        return add("DELETE", path, handler);
    }

    private Router add(String method, String path, Handler handler) {
        routes.add(new Route(method, segments(path), handler));
        return this;
    }

    /** Runs the first matching route, or fails with a 404. */
    public Object dispatch(String method, String path, Map<String, String> query, Map<String, Object> body)
            throws Exception {
        String[] parts = segments(path);
        for (Route route : routes) {
            if (!route.method.equals(method)) {
                continue;
            }
            Map<String, String> params = match(route.pattern, parts);
            if (params != null) {
                return route.handler.handle(new ApiRequest(params, query, body));
            }
        }
        throw ApiException.notFound("No API endpoint matches " + method + " " + path);
    }

    private static Map<String, String> match(String[] pattern, String[] parts) {
        if (pattern.length != parts.length) {
            return null;
        }
        Map<String, String> params = new LinkedHashMap<>();
        for (int i = 0; i < pattern.length; i++) {
            String expected = pattern[i];
            if (expected.startsWith("{") && expected.endsWith("}")) {
                params.put(expected.substring(1, expected.length() - 1), parts[i]);
            } else if (!expected.equals(parts[i])) {
                return null;
            }
        }
        return params;
    }

    private static String[] segments(String path) {
        String clean = path.startsWith("/") ? path.substring(1) : path;
        if (clean.endsWith("/")) {
            clean = clean.substring(0, clean.length() - 1);
        }
        return clean.isEmpty() ? new String[0] : clean.split("/");
    }
}
