package com.fast.campus.model;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public String getRole() { return "TeachingAssistant"; }

    // --- TA Domain operations (Kabeer implements) ---

    public void createAssignment() {
        // TODO: Kabeer — implement assignment creation
    }

    public void readSubmissions() {
        // TODO: Kabeer — implement submission reading
    }

    public void giveEvaluationAndSubmission() {
        // TODO: Kabeer — implement evaluation workflow
    }

    // --- Getters / Setters ---

    public Section getAssignedSection()              { return assignedSection; }
    public void setAssignedSection(Section section)  { this.assignedSection = section; }
}
