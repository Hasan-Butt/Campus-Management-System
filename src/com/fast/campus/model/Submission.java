package com.fast.campus.model;

import com.fast.campus.enums.SubmissionStatus;
import java.time.LocalDate;

/**
 * A student's submission for a given assignment.
 *
 * <p>Owner: Kabeer</p>
 */
public class Submission {

    private String submissionId;
    private Assignment assignment;
    private Student student;
    private LocalDate submissionDate;
    private String content;
    private double marks;
    private Feedback feedback;
    private SubmissionStatus status;

    public Submission(String submissionId, Assignment assignment, Student student, String content) {
        this.submissionId = submissionId;
        this.assignment = assignment;
        this.student = student;
        this.content = content;
        this.submissionDate = LocalDate.now();
        this.status = SubmissionStatus.PENDING;
    }

    // --- Domain operations ---

    public void submit() {
        this.submissionDate = LocalDate.now();
        this.status = isLate() ? SubmissionStatus.LATE : SubmissionStatus.SUBMITTED;
    }

    public boolean isLate() {
        return submissionDate != null && submissionDate.isAfter(assignment.getDeadline());
    }

    public void assignMarks(double marks) {
        this.marks = marks;
        this.status = SubmissionStatus.EVALUATED;
    }

    public void addFeedback(Feedback feedback) {
        this.feedback = feedback;
    }

    // --- Getters ---

    public String getSubmissionId()      { return submissionId; }
    public Assignment getAssignment()    { return assignment; }
    public Student getStudent()          { return student; }
    public LocalDate getSubmissionDate() { return submissionDate; }
    public String getContent()           { return content; }
    public double getMarks()             { return marks; }
    public Feedback getFeedback()        { return feedback; }
    public SubmissionStatus getStatus()  { return status; }
}
