package com.fast.campus.model;

/**
 * Concrete student with no additional teaching responsibilities.
 *
 * <p>Owner: Kabeer</p>
 */
public class NormalStudent extends Student {

    private Section assignedSection;

    public NormalStudent(String id, String name, String email,
                         String phoneNumber, String studentId) {
        super(id, name, email, phoneNumber, studentId);
    }

    @Override
    public String getRole() { return "NormalStudent"; }

    // --- Getters / Setters ---

    public Section getAssignedSection()              { return assignedSection; }
    public void setAssignedSection(Section section)  { this.assignedSection = section; }
}
