package com.fast.campus.exception;

/**
 * Thrown when a submitted request is invalid or cannot be processed.
 */
public class InvalidRequestException extends RequestException {

    public InvalidRequestException(String message) {
        super("Invalid request: " + message);
    }
}
