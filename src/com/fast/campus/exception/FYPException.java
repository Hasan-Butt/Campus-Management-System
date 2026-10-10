package com.fast.campus.exception;

/**
 * Abstract base (UML) — thrown via a concrete subclass when an FYP-related operation fails.
 */
public abstract class FYPException extends CampusException {

    public FYPException(String message) {
        super(message);
    }
}
