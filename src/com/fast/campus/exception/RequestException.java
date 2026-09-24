package com.fast.campus.exception;

/**
 * Thrown when a request-related operation is invalid.
 */
public class RequestException extends CampusException {

    public RequestException(String message) {
        super(message);
    }
}
