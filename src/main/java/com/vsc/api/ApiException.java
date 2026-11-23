package com.vsc.api;

/**
 * An error the API can explain to the browser: HTTP status, the offending input
 * (so a form can highlight the right field) and a human-readable message.
 */
public class ApiException extends Exception {

    private final int status;
    private final String field;

    public ApiException(int status, String field, String message) {
        super(message);
        this.status = status;
        this.field = field;
    }

    public static ApiException badRequest(String field, String message) {
        return new ApiException(400, field, message);
    }

    public static ApiException notFound(String message) {
        return new ApiException(404, null, message);
    }

    public int getStatus() {
        return status;
    }

    /** Name of the request field that failed, or null. */
    public String getField() {
        return field;
    }
}
