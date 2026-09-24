package com.fast.campus.exception;

/**
 * Thrown when a student attempts to submit an assignment after the deadline has passed.
 */
public class SubmissionDeadlineException extends AssessmentException {

    public SubmissionDeadlineException(String assignmentTitle) {
        super("Submission deadline has passed for assignment: " + assignmentTitle);
    }
}
