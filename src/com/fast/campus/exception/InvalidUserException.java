package com.fast.campus.exception;

/**
 * Thrown when user data is invalid (e.g. missing or duplicate student ID).
 * (Concrete subclass, since the UML makes UserException abstract.)
 */
public class InvalidUserException extends UserException {
    public InvalidUserException(String message) {
        super(message);
    }
}
