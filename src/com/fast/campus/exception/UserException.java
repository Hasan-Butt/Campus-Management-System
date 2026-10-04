package com.fast.campus.exception;

/**
 * Abstract base (UML) — thrown via a concrete subclass when a user-related operation fails.
 */
public abstract class UserException extends CampusException {

    public UserException(String message) {
        super(message);
    }
}
