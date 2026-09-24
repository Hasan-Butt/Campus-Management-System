package com.fast.campus.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Full-time permanent instructor who can also supervise FYP groups
 * and assign Teaching Assistants.
 *
 * <p>Owner: Saim</p>
 */
public class PermanentInstructor extends Instructor {

    private List<FYPGroup> supervisedGroups;

    public PermanentInstructor(String id, String name, String email,
                               String phoneNumber, String teacherId) {
        super(id, name, email, phoneNumber, teacherId);
        this.supervisedGroups = new ArrayList<>();
    }

    @Override
    public String getRole() { return "PermanentInstructor"; }

    // --- FYP operations (Saim implements) ---

    public void assignTAAsStudent(NormalStudent student, Section section) {
        // TODO: Saim — implement TA assignment logic
    }

    public List<FYPGroup> viewFYPGroup() {
        return supervisedGroups;
    }

    public void scheduleFYPMeeting(FYPGroup group, FYPMeeting meeting) {
        // TODO: Saim — implement meeting scheduling
    }

    public void evaluateFYPIdea(FYPGroup group) {
        // TODO: Saim — implement FYP idea evaluation
    }

    public void provideFYPFeedback(FYPGroup group, String feedback) {
        // TODO: Saim — implement feedback provision
    }

    // --- Getters ---

    public List<FYPGroup> getSupervisedGroups() { return supervisedGroups; }
}
