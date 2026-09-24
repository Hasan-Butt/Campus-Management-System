package com.fast.campus.exception;

/**
 * Thrown when a user-related operation fails.
 */
public class UserException extends CampusException {

    public UserException(String message) {
        super(message);
    }
}
