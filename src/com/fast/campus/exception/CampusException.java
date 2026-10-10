package com.fast.campus.exception;

/**
 * Abstract root exception (UML) for all campus management system errors.
 * All custom exceptions extend this class.
 */
public abstract class CampusException extends Exception {

    public CampusException(String message) {
        super(message);
    }

    public CampusException(String message, Throwable cause) {
        super(message, cause);
    }
}
