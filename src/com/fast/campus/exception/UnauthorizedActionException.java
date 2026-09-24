package com.fast.campus.exception;

/**
 * Thrown when a user attempts an operation they are not authorized to perform.
 */
public class UnauthorizedActionException extends UserException {

    public UnauthorizedActionException(String actorName, String action) {
        super("Actor '" + actorName + "' is not authorized to perform: " + action);
    }
}
