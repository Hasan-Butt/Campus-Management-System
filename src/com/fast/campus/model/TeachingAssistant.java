package com.fast.campus.model;

import java.util.ArrayList;
import java.util.List;

import com.fast.campus.exception.UnauthorizedActionException;

/**
 * A student who also serves as a Teaching Assistant for a section.
 *
 * <p>Owner: Kabeer</p>
 */
public class TeachingAssistant extends Student {

    private Section assignedSection;

    public TeachingAssistant(String id, String name, String email,
                              String phoneNumber, String studentId) {
        super(id, name, email, phoneNumber, studentId);
    }

    public TeachingAssistant(NormalStudent student) {
        super(student.getId(), student.getName(), student.getEmail(),
              student.getPhoneNumber(), student.getStudentId());
    }

    @Override
    public String getRole() { return "TeachingAssistant"; }

    // --- Helper method ---

    private void checkOwnSection(Assignment assignment, String action) throws UnauthorizedActionException {
        if (assignedSection == null || assignment.getSection() != assignedSection) {
            throw new UnauthorizedActionException(getName(), action);
        }
    }

    // --- TA Domain operations (Kabeer implements) ---

    public List<Submission> viewSubmissions(Assignment assignment) throws UnauthorizedActionException {
        checkOwnSection(assignment, "view submission of " + assignment.getTitle());
        return assignment.getSubmissions();
    }

    // --- Getters / Setters ---

    public Section getAssignedSection()              { return assignedSection; }
    public void setAssignedSection(Section section)  { this.assignedSection = section; }
}
