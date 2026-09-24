package com.fast.campus.exception;

/**
 * Thrown when a course-related operation fails.
 */
public class CourseException extends CampusException {

    public CourseException(String message) {
        super(message);
    }
}
