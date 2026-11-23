package com.vsc.api;

/** Explicit status code for handlers that do not simply return 200. */
public record ApiResponse(int status, Object body) {

    public static ApiResponse created(Object body) {
        return new ApiResponse(201, body);
    }

    public static ApiResponse noContent() {
        return new ApiResponse(204, null);
    }
}
