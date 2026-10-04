package com.fast.campus.exception;

/**
 * Abstract base (UML) — thrown via a concrete subclass when a course-related operation fails.
 */
public abstract class CourseException extends CampusException {

    public CourseException(String message) {
        super(message);
    }
}
