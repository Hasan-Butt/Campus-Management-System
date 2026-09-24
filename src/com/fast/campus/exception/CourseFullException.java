package com.fast.campus.exception;

/**
 * Thrown when a student tries to enroll in a section that has no available seats.
 */
public class CourseFullException extends CourseException {

    public CourseFullException(String sectionId) {
        super("Section " + sectionId + " is full and cannot accept new enrollments.");
    }
}
