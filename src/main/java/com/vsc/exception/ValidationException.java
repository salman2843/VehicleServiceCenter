package com.vsc.exception;

/**
 * Thrown when user input fails a validation rule (blank name, bad phone number,
 * non-positive quantity, ...). The UI catches it and shows a friendly dialog
 * instead of a stack trace.
 */
public class ValidationException extends Exception {

    private final String field;

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    /** Name of the input that failed, e.g. "phone", so the UI can focus it. */
    public String getField() {
        return field;
    }
}
