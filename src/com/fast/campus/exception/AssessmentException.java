package com.fast.campus.exception;

/**
 * Abstract base (UML) — thrown via a concrete subclass when an assessment-related operation fails.
 */
public abstract class AssessmentException extends CampusException {

    public AssessmentException(String message) {
        super(message);
    }
}
