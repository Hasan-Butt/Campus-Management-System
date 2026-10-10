package com.fast.campus.exception;

/**
 * Abstract base (UML) — thrown via a concrete subclass when a request-related operation is invalid.
 */
public abstract class RequestException extends CampusException {

    public RequestException(String message) {
        super(message);
    }
}
