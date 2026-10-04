package com.fast.campus.exception;

/**
 * Thrown when assessment input is invalid (e.g. empty title, non-positive marks, past deadline).
 * (Concrete subclass, since the UML makes AssessmentException abstract.)
 */
public class InvalidAssessmentException extends AssessmentException {
    public InvalidAssessmentException(String message) {
        super(message);
    }
}
