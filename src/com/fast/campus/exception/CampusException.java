package com.fast.campus.exception;

/**
 * Root exception for all campus management system errors.
 * All custom exceptions extend this class.
 */
public class CampusException extends Exception {

    public CampusException(String message) {
        super(message);
    }

    public CampusException(String message, Throwable cause) {
        super(message, cause);
    }
}
