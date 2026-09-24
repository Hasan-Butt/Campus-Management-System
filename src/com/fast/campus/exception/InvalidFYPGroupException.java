package com.fast.campus.exception;

/**
 * Thrown when an FYP group configuration is invalid (e.g., duplicate members, wrong size).
 */
public class InvalidFYPGroupException extends FYPException {

    public InvalidFYPGroupException(String message) {
        super("Invalid FYP group: " + message);
    }
}
