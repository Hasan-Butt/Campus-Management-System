package com.fast.campus.exception;

/**
 * Thrown when an FYP evaluation is invalid or cannot be recorded.
 */
public class InvalidFYPEvaluationException extends FYPException {

    public InvalidFYPEvaluationException(String message) {
        super("Invalid FYP evaluation: " + message);
    }
}
