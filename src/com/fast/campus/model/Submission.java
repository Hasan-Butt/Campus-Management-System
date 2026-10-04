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
        this(submissionId, assignment, student, LocalDate.now(), content, 0, null, SubmissionStatus.PENDING);
    }

    public Submission(String submissionId, Assignment assignment, Student student, LocalDate submissionDate, String content, double marks, Feedback feedback, SubmissionStatus status) {
        this.submissionId = submissionId;
        this.assignment = assignment;
        this.student = student;
        this.submissionDate = submissionDate;
        this.content = content;
        this.marks = marks;
        this.feedback = feedback;
        this.status = status;
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

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
        if (assignment != null && !assignment.getSubmissions().contains(this)) {
            assignment.addSubmission(this);
        }
    }

    @Override
    public String toString() {
        String result = "[" + submissionId + "] " + student.getName() + " (" + student.getStudentId() + ")"
                + " | " + submissionDate + " | " + status;
        if (status == SubmissionStatus.EVALUATED) {
            result += " | " + marks + "/" + assignment.getTotalMarks();
        }
        if (isLate() && status != SubmissionStatus.LATE) {
            result += " (late)";
        }
        if (feedback != null) {
            result += " | feedback: " + feedback.getComments();
        }
        return result;
    }
}
