package com.fast.campus.exception;

/**
 * Thrown when two sections have overlapping schedules causing a time clash.
 */
public class CourseClashException extends CourseException {

    public CourseClashException(String section1, String section2) {
        super("Schedule clash detected between section " + section1 + " and section " + section2 + ".");
    }
}
