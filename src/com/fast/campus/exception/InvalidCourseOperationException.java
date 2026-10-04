package com.fast.campus.exception;

/**
 * Thrown when a course or section operation is invalid (e.g. not found, bad capacity, duplicate).
 * (Concrete subclass, since the UML makes CourseException abstract.)
 */
public class InvalidCourseOperationException extends CourseException {
    public InvalidCourseOperationException(String message) {
        super(message);
    }
}
