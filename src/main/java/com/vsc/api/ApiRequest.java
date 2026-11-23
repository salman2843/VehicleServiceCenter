package com.vsc.api;

import java.util.Map;

/**
 * One decoded API call: path parameters ({@code /jobs/{id}}), query parameters
 * and the parsed JSON body.
 */
public final class ApiRequest {

    private final Map<String, String> pathParams;
    private final Map<String, String> query;
    private final Map<String, Object> body;

    public ApiRequest(Map<String, String> pathParams, Map<String, String> query, Map<String, Object> body) {
        this.pathParams = pathParams;
        this.query = query;
        this.body = body;
    }

    /** Reads a numeric path parameter such as {@code {id}}. */
    public int pathInt(String name) throws ApiException {
        String raw = pathParams.get(name);
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException | NullPointerException e) {
            throw ApiException.badRequest(name, "Expected a numeric id in the URL.");
        }
    }

    public String path(String name) {
        return pathParams.get(name);
    }

    /** Query value, or null when absent/blank. */
    public String query(String name) {
        String value = query.get(name);
        return value == null || value.isBlank() ? null : value.trim();
    }

    public Map<String, Object> body() {
        return body;
    }

    public String text(String key) {
        return Json.text(body, key);
    }

    public Integer integer(String key) {
        return Json.integer(body, key);
    }

    public java.math.BigDecimal decimal(String key) {
        return Json.decimal(body, key);
    }

    /** Same as {@link #integer(String)} but reports a friendly error when missing. */
    public int requiredInt(String key) throws ApiException {
        Integer value = integer(key);
        if (value == null) {
            throw ApiException.badRequest(key, "This value is required.");
        }
        return value;
    }

    public java.math.BigDecimal requiredDecimal(String key) throws ApiException {
        java.math.BigDecimal value = decimal(key);
        if (value == null) {
            throw ApiException.badRequest(key, "This value is required.");
        }
        return value;
    }
}
