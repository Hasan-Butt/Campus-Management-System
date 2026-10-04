package com.fast.campus.model;

import com.fast.campus.enums.SubmissionStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fast.campus.exception.AssessmentException;
import com.fast.campus.exception.InvalidAssessmentException;
import com.fast.campus.exception.UnauthorizedActionException;

/**
 * A student who also serves as a Teaching Assistant for a section.
 *
 * <p>Owner: Kabeer</p>
 */
public class TeachingAssistant extends Student implements Evaluator {

    private Section assignedSection;
    private List<Assignment> createdAssignments = new ArrayList<>();

    public TeachingAssistant(String id, String name, String email,
                              String phoneNumber, String studentId) {
        super(id, name, email, phoneNumber, studentId);
    }

    public TeachingAssistant(NormalStudent student) {
        super(student.getId(), student.getName(), student.getEmail(),
              student.getPhone(), student.getStudentId());
    }

    @Override
    public String getRole() { return "TeachingAssistant"; }

    // --- Evaluator ---

    /** Reports how much grading is still pending on this TA's assignments. */
    @Override
    public void evaluate() {
        for (Assignment assignment : createdAssignments) {
            int ungraded = 0;
            for (Submission submission : assignment.getSubmissions()) {
                if (submission.getStatus() != SubmissionStatus.EVALUATED) {
                    ungraded++;
                }
            }
            System.out.println("  " + assignment.getTitle() + ": " + ungraded + " of "
                    + assignment.getSubmissions().size() + " submission(s) still to evaluate");
        }
    }

    @Override
    public String getEvaluatorId()   { return getStudentId(); }

    @Override
    public String getEvaluatorName() { return getName(); }

    // --- Helper method ---

    private void checkOwnSection(Assignment assignment, String action) throws UnauthorizedActionException {
        if (assignedSection == null || assignment.getSection() != assignedSection) {
            throw new UnauthorizedActionException(getName(), action);
        }
    }

    // --- TA Domain operations (Kabeer implements) ---

    public Assignment createAssignment(String title, String description, LocalDate deadline, double totalMarks)
            throws UnauthorizedActionException, AssessmentException {
        if (assignedSection == null) {
            throw new UnauthorizedActionException(getName(), "create assignment " + title);
        }
        if (title == null || title.isBlank()) {
            throw new InvalidAssessmentException("Assignment title cannot be empty");
        }
        if (totalMarks <= 0) {
            throw new InvalidAssessmentException("Total marks must be greater than 0");
        }
        if (deadline == null || deadline.isBefore(LocalDate.now())) {
            throw new InvalidAssessmentException("Deadline cannot be in the past");
        }
        String assignmentId = "A-" + System.currentTimeMillis();
        Assignment assignment = new Assignment(assignmentId, title, description, deadline, totalMarks, assignedSection, this);
        createdAssignments.add(assignment);
        return assignment;
    }

    public List<Submission> viewSubmissions(Assignment assignment) throws UnauthorizedActionException {
        checkOwnSection(assignment, "view submission of " + assignment.getTitle());
        return assignment.getSubmissions();
    }

    public List<Submission> viewLateSubmissions(Assignment assignment) throws UnauthorizedActionException {
        List<Submission> allSubmissions = viewSubmissions(assignment);
        List<Submission> lateSubmissions = new ArrayList<>();
        for (Submission submission : allSubmissions) {
            if (submission.isLate()) {
                lateSubmissions.add(submission);
            }
        }
        return lateSubmissions;
    }

    public void evaluateSubmission(Submission submission, double marks) throws UnauthorizedActionException, AssessmentException {
        checkOwnSection(submission.getAssignment(), "evaluate submission " + submission.getSubmissionId());
        if (marks < 0 || marks > submission.getAssignment().getTotalMarks()) {
            throw new InvalidAssessmentException("Marks must be between 0 and " + submission.getAssignment().getTotalMarks());
        }
        submission.assignMarks(marks);
    }

    public void giveFeedback(Submission submission, String comments) throws UnauthorizedActionException {
        checkOwnSection(submission.getAssignment(), "give feedback on " + submission.getSubmissionId());
        Feedback feedback = new Feedback("F-" + System.currentTimeMillis(), this, comments);
        submission.addFeedback(feedback);
    }

    // --- Getters / Setters ---

    public List<Assignment> getCreatedAssignments()  { return createdAssignments; }
    public Section getAssignedSection()              { return assignedSection; }
    public void setAssignedSection(Section section)  { this.assignedSection = section; }
}
